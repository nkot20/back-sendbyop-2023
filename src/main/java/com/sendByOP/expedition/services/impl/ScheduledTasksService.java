package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.models.entities.PlatformSettings;
import com.sendByOP.expedition.models.enums.FlightStatus;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.repositories.FlightRepository;
import com.sendByOP.expedition.repositories.PlatformSettingsRepository;
import com.sendByOP.expedition.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service gérant les tâches planifiées (CRON jobs)
 * - Expiration des vols
 * - Confirmation automatique de réception
 * - Fermeture des avis
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasksService {
    
    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PlatformSettingsRepository platformSettingsRepository;
    
    /**
     * CRON JOB 1: Marquer automatiquement les vols comme EXPIRED
     * après leur date/heure d'arrivée
     * 
     * Exécuté toutes les heures
     */
    @Scheduled(cron = "0 0 * * * *") // Toutes les heures à la minute 0
    @Transactional
    public void expireFlights() {
        log.info("=== CRON: Vérification des vols expirés ===");
        
        try {
            Date now = new Date();
            
            // Trouver tous les vols actifs dont la date d'arrivée est passée
            List<Flight> expiredFlights = flightRepository.findAll().stream()
                    .filter(flight -> flight.getStatus() == FlightStatus.ACTIVE)
                    .filter(flight -> flight.getArrivalDate() != null)
                    .filter(flight -> flight.getArrivalDate().before(now))
                    .toList();
            
            if (expiredFlights.isEmpty()) {
                log.info("Aucun vol expiré trouvé");
                return;
            }
            
            // Marquer comme EXPIRED
            expiredFlights.forEach(flight -> {
                flight.setStatus(FlightStatus.EXPIRED);
                flightRepository.save(flight);
                log.info("Vol {} marqué comme EXPIRED (arrivée: {})", 
                        flight.getFlightId(), flight.getArrivalDate());
            });
            
            log.info("=== {} vol(s) marqué(s) comme EXPIRED ===", expiredFlights.size());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'expiration des vols: {}", e.getMessage(), e);
        }
    }
    
    /**
     * CRON JOB 2: Confirmer automatiquement la réception du colis
     * si le destinataire ne confirme pas dans les 72h (configurable)
     * après marquage "Livré" par le voyageur
     * 
     * Exécuté toutes les 6 heures
     */
    @Scheduled(cron = "0 0 */6 * * *") // Toutes les 6 heures
    @Transactional
    public void autoConfirmReception() {
        log.info("=== CRON: Confirmation automatique de réception ===");
        
        try {
            PlatformSettings settings = platformSettingsRepository.findAll().stream()
                    .findFirst()
                    .orElse(null);
            
            if (settings == null) {
                log.warn("Aucun paramètre de plateforme trouvé, utilisation valeur par défaut: 72h");
                settings = new PlatformSettings();
                settings.setReceptionConfirmationHours(72);
            }
            
            LocalDateTime deadline = LocalDateTime.now()
                    .minusHours(settings.getReceptionConfirmationHours());
            
            // Trouver toutes les réservations livrées mais non confirmées depuis plus de 72h
            List<Booking> bookingsToConfirm = bookingRepository.findAll().stream()
                    .filter(booking -> "PARCEL_DELIVERED_TO_RECEIVER".equals(booking.getStatus()) ||
                                      "DELIVERED".equals(booking.getStatus()))
                    .filter(booking -> booking.getUpdatedAt() != null)
                    .filter(booking -> {
                        LocalDateTime updatedAt = booking.getUpdatedAt()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        return updatedAt.isBefore(deadline);
                    })
                    .toList();
            
            if (bookingsToConfirm.isEmpty()) {
                log.info("Aucune réservation à confirmer automatiquement");
                return;
            }
            
            // Confirmer automatiquement
            bookingsToConfirm.forEach(booking -> {
                booking.setStatus(com.sendByOP.expedition.models.enums.BookingStatus.CONFIRMED_BY_RECEIVER);
                booking.setCustomerReceptionStatus(1); // Reçu
                bookingRepository.save(booking);
                
                log.info("Réservation {} confirmée automatiquement (présomption de bonne réception)", 
                        booking.getId());
                
                // TODO: Déclencher le versement au voyageur
                // payoutService.initiatePayoutToTraveler(booking);
            });
            
            log.info("=== {} réservation(s) confirmée(s) automatiquement ===", 
                    bookingsToConfirm.size());
            
        } catch (Exception e) {
            log.error("Erreur lors de la confirmation automatique: {}", e.getMessage(), e);
        }
    }
    
    /**
     * CRON JOB 3: Fermer la possibilité de laisser un avis
     * 90 jours (configurable) après CONFIRMED_BY_RECEIVER
     * 
     * Exécuté tous les jours à minuit
     */
    @Scheduled(cron = "0 0 0 * * *") // Tous les jours à minuit
    @Transactional
    public void closeExpiredReviews() {
        log.info("=== CRON: Fermeture des périodes d'avis expirées ===");
        
        try {
            PlatformSettings settings = platformSettingsRepository.findAll().stream()
                    .findFirst()
                    .orElse(null);
            
            if (settings == null) {
                log.warn("Aucun paramètre de plateforme trouvé, utilisation valeur par défaut: 90 jours");
                settings = new PlatformSettings();
                settings.setReviewDeadlineDays(90);
            }
            
            LocalDateTime deadline = LocalDateTime.now()
                    .minusDays(settings.getReviewDeadlineDays());
            
            // Trouver toutes les réservations confirmées depuis plus de 90 jours
            // qui n'ont pas encore d'avis
            List<Booking> expiredBookings = bookingRepository.findAll().stream()
                    .filter(booking -> "CONFIRMED_BY_RECEIVER".equals(booking.getStatus()) ||
                                      "PICKED_UP".equals(booking.getStatus()))
                    .filter(booking -> booking.getUpdatedAt() != null)
                    .filter(booking -> {
                        LocalDateTime confirmedAt = booking.getUpdatedAt()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        return confirmedAt.isBefore(deadline);
                    })
                    .filter(booking -> {
                        // Vérifier qu'il n'y a pas déjà un avis
                        return reviewRepository.findByBookingId(booking.getId()).isEmpty();
                    })
                    .toList();
            
            if (expiredBookings.isEmpty()) {
                log.info("Aucune période d'avis expirée");
                return;
            }
            
            // Marquer ces réservations (vous pouvez ajouter un flag si nécessaire)
            log.info("=== {} réservation(s) dont la période d'avis est expirée ===", 
                    expiredBookings.size());
            
            expiredBookings.forEach(booking -> {
                log.info("Réservation {} - Période d'avis expirée (confirmée le: {})", 
                        booking.getId(), booking.getUpdatedAt());
            });
            
        } catch (Exception e) {
            log.error("Erreur lors de la fermeture des avis: {}", e.getMessage(), e);
        }
    }
    
    /**
     * CRON JOB 4: Vérifier et annuler les réservations dont le délai de paiement est expiré
     * 
     * Exécuté toutes les 30 minutes
     */
    @Scheduled(cron = "0 */30 * * * *") // Toutes les 30 minutes
    @Transactional
    public void cancelUnpaidBookings() {
        log.info("=== CRON: Vérification des réservations impayées ===");
        
        try {
            PlatformSettings settings = platformSettingsRepository.findAll().stream()
                    .findFirst()
                    .orElse(null);
            
            if (settings == null) {
                log.warn("Aucun paramètre de plateforme trouvé, utilisation valeur par défaut: 12h");
                settings = new PlatformSettings();
                settings.setPaymentTimeoutHours(12);
            }
            
            LocalDateTime deadline = LocalDateTime.now()
                    .minusHours(settings.getPaymentTimeoutHours());
            
            // Trouver toutes les réservations confirmées mais non payées depuis plus de 12h
            List<Booking> expiredBookings = bookingRepository.findAll().stream()
                    .filter(booking -> "CONFIRMED_UNPAID".equals(booking.getStatus()))
                    .filter(booking -> booking.getCreatedAt() != null)
                    .filter(booking -> {
                        LocalDateTime createdAt = booking.getCreatedAt()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        return createdAt.isBefore(deadline);
                    })
                    .toList();
            
            if (expiredBookings.isEmpty()) {
                log.info("Aucune réservation impayée à annuler");
                return;
            }
            
            // Annuler les réservations
            expiredBookings.forEach(booking -> {
                booking.setStatus(com.sendByOP.expedition.models.enums.BookingStatus.CANCELLED_PAYMENT_TIMEOUT);
                booking.setCancelled(1);
                bookingRepository.save(booking);
                
                log.info("Réservation {} annulée pour délai de paiement dépassé", 
                        booking.getId());
                
                // TODO: Libérer le poids réservé sur le vol
                // flightService.releaseWeight(booking.getFlight(), booking.getTotalWeight());
            });
            
            log.info("=== {} réservation(s) annulée(s) pour délai de paiement dépassé ===", 
                    expiredBookings.size());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'annulation des réservations impayées: {}", e.getMessage(), e);
        }
    }
}
