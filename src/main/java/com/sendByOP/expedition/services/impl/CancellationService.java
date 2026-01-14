package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.PlatformSettings;
import com.sendByOP.expedition.models.entities.Transaction;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.repositories.PlatformSettingsRepository;
import com.sendByOP.expedition.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion des annulations et remboursements
 * Applique les règles métier configurables
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CancellationService {
    
    private final BookingRepository bookingRepository;
    private final TransactionRepository transactionRepository;
    private final PlatformSettingsRepository platformSettingsRepository;
    
    /**
     * Calcule le montant du remboursement en fonction des règles métier
     * 
     * Règles:
     * - Avant paiement: annulation gratuite (100%)
     * - Après paiement et avant 4h du vol: remboursement 90%
     * - Après 4h avant vol: impossible d'annuler (0%)
     * 
     * @param bookingId ID de la réservation
     * @return Map contenant les détails du remboursement
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calculateRefund(Integer bookingId) throws SendByOpException {
        log.info("Calcul du remboursement pour la réservation {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new SendByOpException(
                        ErrorInfo.RESOURCE_NOT_FOUND, 
                        "Réservation non trouvée"));
        
        PlatformSettings settings = getSettings();
        
        Map<String, Object> refundDetails = new HashMap<>();
        
        // Vérifier si la réservation est déjà annulée
        if (booking.getCancelled() == 1) {
            throw new SendByOpException(
                    ErrorInfo.INVALID_DATA, 
                    "Cette réservation est déjà annulée");
        }
        
        // Vérifier le statut de paiement
        boolean isPaid = isPaid(booking);
        
        if (!isPaid) {
            // Avant paiement: annulation gratuite (100%)
            refundDetails.put("canCancel", true);
            refundDetails.put("refundAmount", BigDecimal.ZERO);
            refundDetails.put("refundPercentage", BigDecimal.ZERO);
            refundDetails.put("insuranceRefund", BigDecimal.ZERO);
            refundDetails.put("reason", "Annulation avant paiement - gratuite");
            return refundDetails;
        }
        
        // Récupérer la transaction
        List<Transaction> transactions = transactionRepository
                .findByBookingIdOrderByCreatedAtDesc(bookingId);
        
        if (transactions.isEmpty()) {
            throw new SendByOpException(
                    ErrorInfo.RESOURCE_NOT_FOUND, 
                    "Transaction non trouvée pour cette réservation");
        }
        
        Transaction transaction = transactions.get(0);
        BigDecimal totalPaid = transaction.getAmount();
        
        // Calculer le temps restant avant le vol
        Date flightDate = booking.getFlight().getDepartureDate();
        LocalDateTime flightDateTime = flightDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilFlight = Duration.between(now, flightDateTime).toHours();
        
        log.info("Heures avant le vol: {}", hoursUntilFlight);
        
        // Appliquer les règles métier
        if (hoursUntilFlight < settings.getCriticalCancellationHours()) {
            // Moins de 4h avant le vol: impossible d'annuler
            refundDetails.put("canCancel", false);
            refundDetails.put("refundAmount", BigDecimal.ZERO);
            refundDetails.put("refundPercentage", BigDecimal.ZERO);
            refundDetails.put("insuranceRefund", BigDecimal.ZERO);
            refundDetails.put("reason", String.format(
                    "Impossible d'annuler moins de %d heures avant le vol",
                    settings.getCriticalCancellationHours()));
            return refundDetails;
        }
        
        // Plus de 4h avant le vol: remboursement 90%
        BigDecimal refundRate = settings.getRefundRateBeforeDeadline()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        
        BigDecimal refundAmount = totalPaid.multiply(refundRate)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Remboursement de l'assurance (si non utilisée)
        BigDecimal insuranceRefund = settings.getInsuranceAmount();
        BigDecimal totalRefund = refundAmount.add(insuranceRefund);
        
        refundDetails.put("canCancel", true);
        refundDetails.put("refundAmount", refundAmount);
        refundDetails.put("refundPercentage", settings.getRefundRateBeforeDeadline());
        refundDetails.put("insuranceRefund", insuranceRefund);
        refundDetails.put("totalRefund", totalRefund);
        refundDetails.put("amountPaid", totalPaid);
        refundDetails.put("reason", String.format(
                "Remboursement de %.0f%% car annulation plus de %d heures avant le vol",
                settings.getRefundRateBeforeDeadline().doubleValue(),
                settings.getCriticalCancellationHours()));
        
        return refundDetails;
    }
    
    /**
     * Effectue l'annulation de la réservation et le remboursement
     * 
     * @param bookingId ID de la réservation
     * @param customerId ID du client (pour vérification)
     * @return Détails du remboursement effectué
     */
    @Transactional
    public Map<String, Object> cancelBookingWithRefund(Integer bookingId, Integer customerId) 
            throws SendByOpException {
        log.info("Annulation de la réservation {} par le client {}", bookingId, customerId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new SendByOpException(
                        ErrorInfo.RESOURCE_NOT_FOUND, 
                        "Réservation non trouvée"));
        
        // Vérifier que c'est bien le client propriétaire
        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new SendByOpException(
                    ErrorInfo.UNAUTHORIZED, 
                    "Vous n'êtes pas autorisé à annuler cette réservation");
        }
        
        // Calculer le remboursement
        Map<String, Object> refundDetails = calculateRefund(bookingId);
        
        if (!(boolean) refundDetails.get("canCancel")) {
            throw new SendByOpException(
                    ErrorInfo.INVALID_DATA, 
                    (String) refundDetails.get("reason"));
        }
        
        // Marquer la réservation comme annulée
        booking.setCancelled(1);
        booking.setStatus(com.sendByOP.expedition.models.enums.BookingStatus.CANCELLED_BY_CLIENT);
        bookingRepository.save(booking);
        
        // TODO: Créer une transaction de remboursement
        // TODO: Libérer le poids sur le vol
        // TODO: Envoyer notification au client et au voyageur
        
        log.info("Réservation {} annulée avec succès. Remboursement: {}", 
                bookingId, refundDetails.get("totalRefund"));
        
        return refundDetails;
    }
    
    /**
     * Calcule le gain net du voyageur après commission et frais
     * 
     * @param totalAmount Montant total payé par le client
     * @return Map avec les détails du calcul
     */
    public Map<String, Object> calculateTravelerEarnings(BigDecimal totalAmount) {
        PlatformSettings settings = getSettings();
        
        // Commission plateforme (15%)
        BigDecimal commissionRate = settings.getCommissionPercentage()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal commission = totalAmount.multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Assurance
        BigDecimal insurance = settings.getInsuranceAmount();
        
        // Gain net = Total - Commission - Assurance
        BigDecimal netEarnings = totalAmount.subtract(commission).subtract(insurance);
        
        Map<String, Object> earnings = new HashMap<>();
        earnings.put("totalAmount", totalAmount);
        earnings.put("commission", commission);
        earnings.put("commissionPercentage", settings.getCommissionPercentage());
        earnings.put("insurance", insurance);
        earnings.put("netEarnings", netEarnings);
        
        log.info("Calcul gains voyageur: Total={}, Commission={}, Net={}", 
                totalAmount, commission, netEarnings);
        
        return earnings;
    }
    
    /**
     * Vérifie si une réservation est payée
     */
    private boolean isPaid(Booking booking) {
        return "PAID".equals(booking.getStatus()) ||
               "PARCEL_PICKED_UP_BY_TRAVELER".equals(booking.getStatus()) ||
               "PARCEL_DELIVERED_TO_RECEIVER".equals(booking.getStatus()) ||
               "DELIVERED".equals(booking.getStatus()) ||
               "CONFIRMED_BY_RECEIVER".equals(booking.getStatus());
    }
    
    /**
     * Récupère les paramètres de la plateforme
     */
    private PlatformSettings getSettings() {
        return platformSettingsRepository.findAll().stream()
                .findFirst()
                .orElse(new PlatformSettings());
    }
}
