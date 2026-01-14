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
 * Provider pour paiement PayPal
 * SIMULATION - À remplacer par l'intégration réelle avec l'API PayPal
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PayPalProvider implements PaymentProvider {
    
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.PAYPAL;
    }
    
    @Override
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, Transaction transaction) 
            throws SendByOpException {
        
        log.info("Initiation paiement PayPal pour transaction: {}", 
                transaction.getTransactionReference());
        
        // Validation de l'email PayPal
        if (request.getPaypalEmail() == null || !request.getPaypalEmail().contains("@")) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, "Email PayPal invalide");
        }
        
        try {
            // SIMULATION - Dans la vraie implémentation, appeler l'API PayPal
            String externalTransactionId = "PP-" + UUID.randomUUID().toString();
            String paymentUrl = "https://www.paypal.com/checkoutnow?token=" + externalTransactionId;
            
            log.info("Paiement PayPal initié - External ID: {}", externalTransactionId);
            
            return PaymentResponse.builder()
                    .transactionId(transaction.getId())
                    .transactionReference(transaction.getTransactionReference())
                    .bookingId(request.getBookingId())
                    .amount(request.getAmount())
                    .paymentMethod(PaymentMethod.PAYPAL)
                    .status(TransactionStatus.PENDING)
                    .statusMessage("En attente de validation PayPal")
                    .createdAt(transaction.getCreatedAt())
                    .paymentUrl(paymentUrl)
                    .requiresConfirmation(true)
                    .confirmationMessage("Vous allez être redirigé vers PayPal pour compléter le paiement")
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors de l'initiation du paiement PayPal: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de l'initiation du paiement PayPal: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResponse checkPaymentStatus(Transaction transaction) throws SendByOpException {
        log.info("Vérification statut paiement PayPal: {}", transaction.getTransactionReference());
        
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .bookingId(transaction.getBooking().getId())
                .amount(transaction.getAmount())
                .paymentMethod(PaymentMethod.PAYPAL)
                .status(transaction.getStatus())
                .statusMessage("Statut actuel du paiement")
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
    
    @Override
    public String processWebhookNotification(WebhookPaymentNotification notification) 
            throws SendByOpException {
        
        log.info("Traitement webhook PayPal pour transaction: {}", 
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
        log.info("Vérification signature webhook PayPal");
        
        if (signature == null || signature.isEmpty()) {
            log.warn("Signature manquante dans le webhook PayPal");
            return false;
        }
        
        // Pour la simulation
        return signature.startsWith("PP_") || signature.equals("SIMULATED_SIGNATURE");
    }
    
    @Override
    public boolean cancelPayment(Transaction transaction) throws SendByOpException {
        log.info("Annulation paiement PayPal: {}", transaction.getTransactionReference());
        
        if (transaction.getStatus() == TransactionStatus.PENDING || 
            transaction.getStatus() == TransactionStatus.PROCESSING) {
            log.info("Paiement PayPal annulé avec succès");
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean refundPayment(Transaction transaction, String reason) throws SendByOpException {
        log.info("Remboursement paiement PayPal: {} - Raison: {}", 
                transaction.getTransactionReference(), reason);
        
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            log.info("Remboursement PayPal initié avec succès");
            return true;
        }
        
        return false;
    }
}
