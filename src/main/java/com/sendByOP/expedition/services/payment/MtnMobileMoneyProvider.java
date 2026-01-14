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

import java.util.UUID;

/**
 * Provider pour MTN Mobile Money Cameroun
 * SIMULATION - À remplacer par l'intégration réelle avec l'API MTN MoMo
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MtnMobileMoneyProvider implements PaymentProvider {
    
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.MTN_MOBILE_MONEY;
    }
    
    @Override
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, Transaction transaction) 
            throws SendByOpException {
        
        log.info("Initiation paiement MTN Mobile Money pour transaction: {}", 
                transaction.getTransactionReference());
        
        // Validation du numéro de téléphone
        if (request.getPhoneNumber() == null || !request.getPhoneNumber().matches("^(\\+237)?[0-9]{9}$")) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Numéro de téléphone MTN Mobile Money invalide");
        }
        
        try {
            // SIMULATION - Dans la vraie implémentation, appeler l'API MTN MoMo
            String externalTransactionId = "MTN-" + UUID.randomUUID().toString();
            String ussdCode = generateUSSDCode(request.getAmount().toString());
            
            log.info("Paiement MTN Mobile Money initié - External ID: {}, USSD: {}", 
                    externalTransactionId, ussdCode);
            
            return PaymentResponse.builder()
                    .transactionId(transaction.getId())
                    .transactionReference(transaction.getTransactionReference())
                    .bookingId(request.getBookingId())
                    .amount(request.getAmount())
                    .paymentMethod(PaymentMethod.MTN_MOBILE_MONEY)
                    .status(TransactionStatus.PENDING)
                    .statusMessage("Paiement en attente de confirmation MTN Mobile Money")
                    .createdAt(transaction.getCreatedAt())
                    .ussdCode(ussdCode)
                    .requiresConfirmation(true)
                    .confirmationMessage("Composez le code USSD " + ussdCode + 
                            " pour confirmer le paiement sur votre téléphone MTN Mobile Money")
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors de l'initiation du paiement MTN Mobile Money: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de l'initiation du paiement MTN Mobile Money: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResponse checkPaymentStatus(Transaction transaction) throws SendByOpException {
        log.info("Vérification statut paiement MTN Mobile Money: {}", 
                transaction.getTransactionReference());
        
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .bookingId(transaction.getBooking().getId())
                .amount(transaction.getAmount())
                .paymentMethod(PaymentMethod.MTN_MOBILE_MONEY)
                .status(transaction.getStatus())
                .statusMessage("Statut actuel du paiement")
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
    
    @Override
    public String processWebhookNotification(WebhookPaymentNotification notification) 
            throws SendByOpException {
        
        log.info("Traitement webhook MTN Mobile Money pour transaction: {}", 
                notification.getTransactionReference());
        
        String transactionReference = notification.getTransactionReference();
        if (transactionReference == null || transactionReference.isEmpty()) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Référence de transaction manquante dans le webhook");
        }
        
        return transactionReference;
    }
    
    @Override
    public boolean verifyWebhookSignature(WebhookPaymentNotification notification, String signature) {
        log.info("Vérification signature webhook MTN Mobile Money");
        
        if (signature == null || signature.isEmpty()) {
            log.warn("Signature manquante dans le webhook MTN Mobile Money");
            return false;
        }
        
        // Pour la simulation, accepter toutes les signatures commençant par "MTN_"
        return signature.startsWith("MTN_") || signature.equals("SIMULATED_SIGNATURE");
    }
    
    @Override
    public boolean cancelPayment(Transaction transaction) throws SendByOpException {
        log.info("Annulation paiement MTN Mobile Money: {}", transaction.getTransactionReference());
        
        if (transaction.getStatus() == TransactionStatus.PENDING || 
            transaction.getStatus() == TransactionStatus.PROCESSING) {
            log.info("Paiement MTN Mobile Money annulé avec succès");
            return true;
        }
        
        log.warn("Impossible d'annuler un paiement MTN Mobile Money avec le statut: {}", 
                transaction.getStatus());
        return false;
    }
    
    @Override
    public boolean refundPayment(Transaction transaction, String reason) throws SendByOpException {
        log.info("Remboursement paiement MTN Mobile Money: {} - Raison: {}", 
                transaction.getTransactionReference(), reason);
        
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            log.info("Remboursement MTN Mobile Money initié avec succès");
            return true;
        }
        
        log.warn("Impossible de rembourser un paiement non complété");
        return false;
    }
    
    private String generateUSSDCode(String amount) {
        return "*126*" + amount + "#";
    }
}
