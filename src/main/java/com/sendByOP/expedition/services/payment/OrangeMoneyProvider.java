package com.sendByOP.expedition.services.payment;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.InitiatePaymentRequest;
import com.sendByOP.expedition.models.dto.PaymentResponse;
import com.sendByOP.expedition.models.dto.WebhookPaymentNotification;
import com.sendByOP.expedition.models.entities.Transaction;
import com.sendByOP.expedition.models.enums.PaymentMethod;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Provider pour Orange Money Cameroun
 * SIMULATION - À remplacer par l'intégration réelle avec l'API Orange Money
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrangeMoneyProvider implements PaymentProvider {
    
    // Configuration (à externaliser dans application.properties)
    private static final String MERCHANT_ID = "SENDBYOP_MERCHANT";
    private static final String API_KEY = "SIMULATED_API_KEY";
    private static final String SECRET_KEY = "SIMULATED_SECRET_KEY";
    
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.ORANGE_MONEY;
    }
    
    @Override
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, Transaction transaction) 
            throws SendByOpException {
        
        log.info("Initiation paiement Orange Money pour transaction: {}", transaction.getTransactionReference());
        
        // Validation du numéro de téléphone
        if (request.getPhoneNumber() == null || !request.getPhoneNumber().matches("^(\\+237)?[0-9]{9}$")) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Numéro de téléphone Orange Money invalide");
        }
        
        try {
            // SIMULATION - Dans la vraie implémentation, appeler l'API Orange Money
            String externalTransactionId = "OM-" + UUID.randomUUID().toString();
            String ussdCode = generateUSSDCode(request.getPhoneNumber(), request.getAmount().toString());
            
            log.info("Paiement Orange Money initié - External ID: {}, USSD: {}", 
                    externalTransactionId, ussdCode);
            
            return PaymentResponse.builder()
                    .transactionId(transaction.getId())
                    .transactionReference(transaction.getTransactionReference())
                    .bookingId(request.getBookingId())
                    .amount(request.getAmount())
                    .paymentMethod(PaymentMethod.ORANGE_MONEY)
                    .status(TransactionStatus.PENDING)
                    .statusMessage("Paiement en attente de confirmation Orange Money")
                    .createdAt(transaction.getCreatedAt())
                    .ussdCode(ussdCode)
                    .requiresConfirmation(true)
                    .confirmationMessage("Composez le code USSD " + ussdCode + 
                            " pour confirmer le paiement sur votre téléphone Orange Money")
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors de l'initiation du paiement Orange Money: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de l'initiation du paiement Orange Money: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResponse checkPaymentStatus(Transaction transaction) throws SendByOpException {
        log.info("Vérification statut paiement Orange Money: {}", transaction.getTransactionReference());
        
        // SIMULATION - Dans la vraie implémentation, appeler l'API Orange Money pour vérifier le statut
        
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .bookingId(transaction.getBooking().getId())
                .amount(transaction.getAmount())
                .paymentMethod(PaymentMethod.ORANGE_MONEY)
                .status(transaction.getStatus())
                .statusMessage("Statut actuel du paiement")
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
    
    @Override
    public String processWebhookNotification(WebhookPaymentNotification notification) 
            throws SendByOpException {
        
        log.info("Traitement webhook Orange Money pour transaction: {}", 
                notification.getTransactionReference());
        
        // Extraire la référence de transaction
        String transactionReference = notification.getTransactionReference();
        if (transactionReference == null || transactionReference.isEmpty()) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Référence de transaction manquante dans le webhook");
        }
        
        return transactionReference;
    }
    
    @Override
    public boolean verifyWebhookSignature(WebhookPaymentNotification notification, String signature) {
        // SIMULATION - Dans la vraie implémentation, vérifier la signature HMAC
        // avec la clé secrète fournie par Orange Money
        
        log.info("Vérification signature webhook Orange Money");
        
        if (signature == null || signature.isEmpty()) {
            log.warn("Signature manquante dans le webhook Orange Money");
            return false;
        }
        
        // Pour la simulation, accepter toutes les signatures commençant par "OM_"
        return signature.startsWith("OM_") || signature.equals("SIMULATED_SIGNATURE");
    }
    
    @Override
    public boolean cancelPayment(Transaction transaction) throws SendByOpException {
        log.info("Annulation paiement Orange Money: {}", transaction.getTransactionReference());
        
        // SIMULATION - Dans la vraie implémentation, appeler l'API d'annulation
        if (transaction.getStatus() == TransactionStatus.PENDING || 
            transaction.getStatus() == TransactionStatus.PROCESSING) {
            log.info("Paiement Orange Money annulé avec succès");
            return true;
        }
        
        log.warn("Impossible d'annuler un paiement Orange Money avec le statut: {}", 
                transaction.getStatus());
        return false;
    }
    
    @Override
    public boolean refundPayment(Transaction transaction, String reason) throws SendByOpException {
        log.info("Remboursement paiement Orange Money: {} - Raison: {}", 
                transaction.getTransactionReference(), reason);
        
        // SIMULATION - Dans la vraie implémentation, appeler l'API de remboursement
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            log.info("Remboursement Orange Money initié avec succès");
            return true;
        }
        
        log.warn("Impossible de rembourser un paiement non complété");
        return false;
    }
    
    /**
     * Génère un code USSD simulé pour le paiement
     */
    private String generateUSSDCode(String phoneNumber, String amount) {
        return "#150*1*" + amount + "#";
    }
}
