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
 * Provider pour paiement par carte bancaire
 * SIMULATION - À remplacer par l'intégration réelle avec un gateway de paiement
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCardProvider implements PaymentProvider {
    
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CREDIT_CARD;
    }
    
    @Override
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, Transaction transaction) 
            throws SendByOpException {
        
        log.info("Initiation paiement Carte bancaire pour transaction: {}", 
                transaction.getTransactionReference());
        
        // Validation basique de la carte
        if (request.getCardNumber() == null || request.getCardNumber().length() < 13) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, "Numéro de carte invalide");
        }
        
        if (request.getCvv() == null || request.getCvv().length() < 3) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, "CVV invalide");
        }
        
        try {
            // SIMULATION - Dans la vraie implémentation, appeler le gateway de paiement
            String externalTransactionId = "CARD-" + UUID.randomUUID().toString();
            String paymentUrl = "https://payment.sendbyop.com/card/" + externalTransactionId;
            
            log.info("Paiement par carte initié - External ID: {}", externalTransactionId);
            
            return PaymentResponse.builder()
                    .transactionId(transaction.getId())
                    .transactionReference(transaction.getTransactionReference())
                    .bookingId(request.getBookingId())
                    .amount(request.getAmount())
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .status(TransactionStatus.PENDING)
                    .statusMessage("Paiement en attente de validation")
                    .createdAt(transaction.getCreatedAt())
                    .paymentUrl(paymentUrl)
                    .requiresConfirmation(true)
                    .confirmationMessage("Cliquez sur le lien pour compléter le paiement par carte")
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors de l'initiation du paiement par carte: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de l'initiation du paiement par carte: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentResponse checkPaymentStatus(Transaction transaction) throws SendByOpException {
        log.info("Vérification statut paiement Carte: {}", transaction.getTransactionReference());
        
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .bookingId(transaction.getBooking().getId())
                .amount(transaction.getAmount())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(transaction.getStatus())
                .statusMessage("Statut actuel du paiement")
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
    
    @Override
    public String processWebhookNotification(WebhookPaymentNotification notification) 
            throws SendByOpException {
        
        log.info("Traitement webhook Carte bancaire pour transaction: {}", 
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
        log.info("Vérification signature webhook Carte bancaire");
        
        if (signature == null || signature.isEmpty()) {
            log.warn("Signature manquante dans le webhook");
            return false;
        }
        
        // Pour la simulation
        return signature.startsWith("CARD_") || signature.equals("SIMULATED_SIGNATURE");
    }
    
    @Override
    public boolean cancelPayment(Transaction transaction) throws SendByOpException {
        log.info("Annulation paiement Carte: {}", transaction.getTransactionReference());
        
        if (transaction.getStatus() == TransactionStatus.PENDING || 
            transaction.getStatus() == TransactionStatus.PROCESSING) {
            log.info("Paiement par carte annulé avec succès");
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean refundPayment(Transaction transaction, String reason) throws SendByOpException {
        log.info("Remboursement paiement Carte: {} - Raison: {}", 
                transaction.getTransactionReference(), reason);
        
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            log.info("Remboursement par carte initié avec succès");
            return true;
        }
        
        return false;
    }
}
