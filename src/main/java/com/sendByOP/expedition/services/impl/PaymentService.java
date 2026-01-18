package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.models.entities.Transaction;
import com.sendByOP.expedition.models.enums.BookingStatus;
import com.sendByOP.expedition.models.enums.PaymentMethod;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.repositories.CustomerRepository;
import com.sendByOP.expedition.repositories.TransactionRepository;
import com.sendByOP.expedition.services.payment.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.sendByOP.expedition.utils.DateTimeUtils;

/**
 * Service principal de gestion des paiements
 * Gère l'orchestration des paiements avec les différents providers
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    
    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    
    // Injection de tous les providers de paiement
    private final OrangeMoneyProvider orangeMoneyProvider;
    private final MtnMobileMoneyProvider mtnProvider;
    private final CreditCardProvider creditCardProvider;
    private final PayPalProvider payPalProvider;
    
    private final InvoiceService invoiceService;
    private final SendMailService emailService;
    
    /**
     * Initie un paiement de manière sécurisée avec idempotence
     */
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) throws SendByOpException {
        log.info("Initiation paiement - Booking: {}, Method: {}, Amount: {}", 
                request.getBookingId(), request.getPaymentMethod(), request.getAmount());
        
        // 1. Vérifier l'idempotence
        String idempotencyKey = generateIdempotencyKey(request);
        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            log.warn("Transaction déjà existante avec cette clé d'idempotence: {}", idempotencyKey);
            return buildPaymentResponse(existingTransaction.get());
        }
        
        // 2. Valider la réservation
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                        "Réservation non trouvée"));
        
        // 3. Vérifier que la réservation est confirmée par le voyageur
        if (booking.getStatus() != BookingStatus.CONFIRMED_UNPAID) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Cette réservation ne peut pas être payée dans son état actuel: " + booking.getStatus());
        }
        
        // 4. Vérifier qu'il n'y a pas déjà un paiement complété
        boolean hasCompletedPayment = transactionRepository.existsByBookingIdAndStatus(
                request.getBookingId(), TransactionStatus.COMPLETED);
        if (hasCompletedPayment) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Cette réservation a déjà été payée");
        }
        
        // 5. Récupérer le client
        Customer customer = customerRepository.findByEmail(request.getCustomerEmail())
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                        "Client non trouvé"));
        
        // 6. Vérifier le montant
        if (request.getAmount().compareTo(booking.getTotalPrice()) != 0) {
            log.warn("Montant du paiement ({}) différent du prix de la réservation ({})", 
                    request.getAmount(), booking.getTotalPrice());
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Le montant du paiement ne correspond pas au prix de la réservation");
        }
        
        // 7. Créer la transaction en base de données
        Transaction transaction = Transaction.builder()
                .booking(booking)
                .customer(customer)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .phoneNumber(request.getPhoneNumber())
                .idempotencyKey(idempotencyKey)
                .build();
        
        transaction = transactionRepository.save(transaction);
        log.info("Transaction créée: {}", transaction.getTransactionReference());
        
        // 8. Initier le paiement auprès du provider approprié
        try {
            PaymentProvider provider = getProvider(request.getPaymentMethod());
            PaymentResponse response = provider.initiatePayment(request, transaction);
            
            // 9. Mettre à jour la transaction avec les détails du provider
            if (response.getStatusMessage() != null) {
                transaction.setPaymentDetails(response.getStatusMessage());
                transactionRepository.save(transaction);
            }
            
            log.info("Paiement initié avec succès - Transaction: {}", 
                    transaction.getTransactionReference());
            
            return response;
            
        } catch (Exception e) {
            // En cas d'erreur, marquer la transaction comme échouée
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transactionRepository.save(transaction);
            
            log.error("Erreur lors de l'initiation du paiement: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de l'initiation du paiement: " + e.getMessage());
        }
    }
    
    /**
     * Traite une notification webhook de paiement
     */
    @Transactional
    public void processWebhookNotification(WebhookPaymentNotification notification, 
                                          PaymentMethod paymentMethod, 
                                          String signature) throws SendByOpException {
        
        log.info("Traitement webhook {} - Transaction: {}", 
                paymentMethod, notification.getTransactionReference());
        
        // 1. Vérifier la signature du webhook
        PaymentProvider provider = getProvider(paymentMethod);
        if (!provider.verifyWebhookSignature(notification, signature)) {
            log.error("Signature webhook invalide pour la transaction: {}", 
                    notification.getTransactionReference());
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED, 
                    "Signature webhook invalide");
        }
        
        // 2. Récupérer la transaction
        String transactionRef = provider.processWebhookNotification(notification);
        Transaction transaction = transactionRepository.findByTransactionReference(transactionRef)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                        "Transaction non trouvée: " + transactionRef));
        
        // 3. Vérifier que la transaction n'est pas déjà complétée
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            log.warn("Transaction déjà complétée, webhook ignoré: {}", transactionRef);
            return;
        }
        
        // 4. Mettre à jour le statut de la transaction selon la notification
        TransactionStatus newStatus = mapWebhookStatus(notification.getStatus());
        transaction.setStatus(newStatus);
        transaction.setExternalTransactionId(notification.getExternalTransactionId());
        transaction.setWebhookReceivedAt(LocalDateTime.now());
        
        if ("SUCCESS".equals(notification.getStatus())) {
            transaction.setCompletedAt(LocalDateTime.now());
            
            // 5. Mettre à jour le statut de la réservation
            Booking booking = transaction.getBooking();
            booking.setStatus(BookingStatus.CONFIRMED_PAID);
            bookingRepository.save(booking);
            
            log.info("Paiement complété avec succès - Transaction: {}", transactionRef);
            
            // 6. Générer et envoyer la facture
            try {
                generateAndSendInvoice(transaction);
            } catch (Exception e) {
                log.error("Erreur lors de la génération de la facture: {}", e.getMessage());
                // Ne pas faire échouer le webhook si la facture échoue
            }
            
        } else if ("FAILED".equals(notification.getStatus())) {
            transaction.setErrorMessage(notification.getMessage());
            transaction.setErrorCode(notification.getErrorCode());
            log.warn("Paiement échoué - Transaction: {}, Raison: {}", 
                    transactionRef, notification.getMessage());
        }
        
        transactionRepository.save(transaction);
    }
    
    /**
     * Vérifie le statut d'un paiement
     */
    @Transactional(readOnly = true)
    public PaymentResponse checkPaymentStatus(String transactionReference) throws SendByOpException {
        log.info("Vérification statut paiement: {}", transactionReference);
        
        Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                        "Transaction non trouvée"));
        
        // Si la transaction est en attente, vérifier auprès du provider
        if (transaction.getStatus() == TransactionStatus.PENDING || 
            transaction.getStatus() == TransactionStatus.PROCESSING) {
            
            try {
                PaymentProvider provider = getProvider(transaction.getPaymentMethod());
                return provider.checkPaymentStatus(transaction);
            } catch (Exception e) {
                log.error("Erreur lors de la vérification du statut: {}", e.getMessage());
            }
        }
        
        return buildPaymentResponse(transaction);
    }
    
    /**
     * Récupère l'historique des transactions d'une réservation
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getBookingTransactions(Integer bookingId) {
        log.info("Récupération des transactions pour la réservation: {}", bookingId);
        
        List<Transaction> transactions = transactionRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        List<PaymentResponse> responses = new ArrayList<>();
        
        for (Transaction transaction : transactions) {
            responses.add(buildPaymentResponse(transaction));
        }
        
        return responses;
    }
    
    /**
     * Génère et envoie la facture par email
     */
    private void generateAndSendInvoice(Transaction transaction) {
        try {
            log.info("Génération de la facture pour la transaction: {}", 
                    transaction.getTransactionReference());
            
            // Générer la facture PDF
            byte[] invoicePdf = invoiceService.generateInvoice(transaction);
            
            // Envoyer par email
            String customerEmail = transaction.getCustomer().getEmail();
            String subject = "Facture SendByOp - " + transaction.getTransactionReference();
            String body = buildInvoiceEmailBody(transaction);
            
            emailService.sendEmailWithAttachment(
                    customerEmail, 
                    subject, 
                    body, 
                    invoicePdf, 
                    "facture_" + transaction.getTransactionReference() + ".pdf"
            );
            
            // Marquer la facture comme envoyée
            transaction.setInvoiceSent(true);
            transactionRepository.save(transaction);
            
            log.info("Facture envoyée avec succès à: {}", customerEmail);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la facture: {}", e.getMessage());
        }
    }
    
    /**
     * Récupère le provider approprié selon la méthode de paiement
     */
    private PaymentProvider getProvider(PaymentMethod paymentMethod) throws SendByOpException {
        return switch (paymentMethod) {
            case ORANGE_MONEY -> orangeMoneyProvider;
            case MTN_MOBILE_MONEY -> mtnProvider;
            case CREDIT_CARD -> creditCardProvider;
            case PAYPAL -> payPalProvider;
            default -> throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Méthode de paiement non supportée: " + paymentMethod);
        };
    }
    
    /**
     * Génère une clé d'idempotence pour éviter les doubles paiements
     */
    private String generateIdempotencyKey(InitiatePaymentRequest request) {
        return String.format("PAY-%d-%s-%s", 
                request.getBookingId(), 
                request.getPaymentMethod().getCode(),
                request.getCustomerEmail());
    }
    
    /**
     * Convertit le statut du webhook en TransactionStatus
     */
    private TransactionStatus mapWebhookStatus(String webhookStatus) {
        return switch (webhookStatus) {
            case "SUCCESS" -> TransactionStatus.COMPLETED;
            case "FAILED" -> TransactionStatus.FAILED;
            case "PENDING" -> TransactionStatus.PROCESSING;
            default -> TransactionStatus.PENDING;
        };
    }
    
    /**
     * Construit une réponse de paiement à partir d'une transaction
     */
    private PaymentResponse buildPaymentResponse(Transaction transaction) {
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .bookingId(transaction.getBooking().getId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .statusMessage(getStatusMessage(transaction.getStatus()))
                .createdAt(DateTimeUtils.dateToLocalDateTime(transaction.getCreatedAt()))
                .completedAt(transaction.getCompletedAt())
                .build();
    }
    
    /**
     * Retourne un message descriptif pour chaque statut
     */
    private String getStatusMessage(TransactionStatus status) {
        return switch (status) {
            case PENDING -> "Paiement en attente de confirmation";
            case PROCESSING -> "Paiement en cours de traitement";
            case COMPLETED -> "Paiement complété avec succès";
            case FAILED -> "Le paiement a échoué";
            case CANCELLED -> "Le paiement a été annulé";
            case REFUNDED -> "Le paiement a été remboursé";
        };
    }
    
    /**
     * Construit le corps de l'email pour l'envoi de la facture
     */
    private String buildInvoiceEmailBody(Transaction transaction) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #FF6B35, #F9A826); color: white; padding: 30px; text-align: center; }
                        .content { background: #f9f9f9; padding: 30px; }
                        .info-box { background: white; border-left: 4px solid #FF6B35; padding: 15px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✅ Paiement Confirmé</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Nous confirmons la réception de votre paiement pour votre réservation SendByOp.</p>
                            <div class="info-box">
                                <strong>Référence de transaction :</strong> %s<br>
                                <strong>Montant payé :</strong> %s FCFA<br>
                                <strong>Méthode de paiement :</strong> %s<br>
                                <strong>Date :</strong> %s
                            </div>
                            <p>Vous trouverez votre facture en pièce jointe de cet email.</p>
                            <p>Merci d'avoir choisi SendByOp pour vos envois !</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 SendByOp. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                transaction.getCustomer().getFirstName() + " " + transaction.getCustomer().getLastName(),
                transaction.getTransactionReference(),
                transaction.getAmount(),
                transaction.getPaymentMethod().getDisplayName(),
                transaction.getCompletedAt() != null ? transaction.getCompletedAt().toString() : "En cours"
        );
    }
    
    /**
     * Récupère l'historique des paiements d'un client (paginé)
     */
    public PagePaymentHistoryDto getPaymentHistory(String customerEmail, int page, int size) {
        log.info("Récupération de l'historique paginé des paiements pour {}", customerEmail);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "completedAt"));
        Page<Transaction> transactionsPage = transactionRepository.findByCustomerEmailOrderByCreatedAtDesc(customerEmail, pageable);
        
        List<PaymentHistoryDto> historyList = transactionsPage.getContent().stream()
                .map(this::convertToPaymentHistoryDto)
                .collect(Collectors.toList());
        
        return PagePaymentHistoryDto.builder()
                .content(historyList)
                .totalElements((int) transactionsPage.getTotalElements())
                .totalPages(transactionsPage.getTotalPages())
                .size(transactionsPage.getSize())
                .number(transactionsPage.getNumber())
                .first(transactionsPage.isFirst())
                .last(transactionsPage.isLast())
                .build();
    }
    
    /**
     * Récupère l'historique complet des paiements d'un client
     */
    public List<PaymentHistoryDto> getPaymentHistoryAll(String customerEmail) {
        log.info("Récupération de l'historique complet des paiements pour {}", customerEmail);
        
        List<Transaction> transactions = transactionRepository.findByCustomerEmailOrderByCreatedAtDesc(customerEmail);
        
        return transactions.stream()
                .map(this::convertToPaymentHistoryDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les statistiques de paiement d'un client
     */
    public PaymentStatsDto getPaymentStats(String customerEmail) {
        log.info("Récupération des statistiques de paiement pour {}", customerEmail);
        
        List<Transaction> transactions = transactionRepository.findByCustomerEmailOrderByCreatedAtDesc(customerEmail);
        
        int totalPayments = transactions.size();
        int completedPayments = (int) transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .count();
        int pendingPayments = (int) transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.PENDING || t.getStatus() == TransactionStatus.PROCESSING)
                .count();
        
        double totalAmount = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        
        double averageAmount = completedPayments > 0 ? totalAmount / completedPayments : 0.0;
        
        return PaymentStatsDto.builder()
                .totalPayments(totalPayments)
                .completedPayments(completedPayments)
                .pendingPayments(pendingPayments)
                .totalAmount(totalAmount)
                .averageAmount(averageAmount)
                .build();
    }
    
    /**
     * Convertit une Transaction en PaymentHistoryDto
     */
    private PaymentHistoryDto convertToPaymentHistoryDto(Transaction transaction) {
        Booking booking = transaction.getBooking();
        Flight flight = booking != null ? booking.getFlight() : null;
        
        PaymentHistoryDto.FlightInfoDto flightInfo = null;
        if (flight != null) {
            flightInfo = PaymentHistoryDto.FlightInfoDto.builder()
                    .departureCityName(flight.getDepartureAirport() != null && flight.getDepartureAirport().getCity() != null ? 
                            flight.getDepartureAirport().getCity().getName() : "N/A")
                    .departureAirportCode(flight.getDepartureAirport() != null ? 
                            flight.getDepartureAirport().getIataCode() : "N/A")
                    .arrivalCityName(flight.getArrivalAirport() != null && flight.getArrivalAirport().getCity() != null ? 
                            flight.getArrivalAirport().getCity().getName() : "N/A")
                    .arrivalAirportCode(flight.getArrivalAirport() != null ? 
                            flight.getArrivalAirport().getIataCode() : "N/A")
                    .departureDate(flight.getDepartureDate() != null ? flight.getDepartureDate().toString() : "N/A")
                    .build();
        }
        
        String description = String.format("Paiement pour réservation #%d", booking != null ? booking.getId() : 0);
        
        return PaymentHistoryDto.builder()
                .id(transaction.getId().longValue())
                .bookingId(booking != null ? booking.getId() : null)
                .amount(transaction.getAmount().doubleValue())
                .paymentType(transaction.getPaymentMethod().name())
                .paymentDate(transaction.getCompletedAt() != null ? transaction.getCompletedAt() : DateTimeUtils.dateToLocalDateTime(transaction.getCreatedAt()))
                .status(transaction.getStatus().name())
                .statusDisplayName(transaction.getStatus().getDisplayName())
                .description(description)
                .paymentMethod(transaction.getPaymentMethod().getDisplayName())
                .transactionReference(transaction.getTransactionReference())
                .flightNumber(flight != null && flight.getFlightId() != null ? "VOL-" + flight.getFlightId() : "N/A")
                .departureCity(flight != null && flight.getDepartureAirport() != null && flight.getDepartureAirport().getCity() != null ? 
                        flight.getDepartureAirport().getCity().getName() : "N/A")
                .arrivalCity(flight != null && flight.getArrivalAirport() != null && flight.getArrivalAirport().getCity() != null ? 
                        flight.getArrivalAirport().getCity().getName() : "N/A")
                .departureDate(flight != null && flight.getDepartureDate() != null ? flight.getDepartureDate().toString() : "N/A")
                .flight(flightInfo)
                .build();
    }
}
