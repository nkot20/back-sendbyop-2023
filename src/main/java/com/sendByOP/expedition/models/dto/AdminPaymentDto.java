package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.PaymentMethod;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les paiements dans le panel d'administration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentDto {
    
    private Integer id;
    private String transactionReference;
    private String externalTransactionId;
    
    // Informations de réservation
    private Integer bookingId;
    private String bookingReference;
    
    // Informations client
    private Integer customerId;
    private String customerName;
    private String customerEmail;
    
    // Montant et devise
    private BigDecimal amount;
    private String currency;
    
    // Méthode et statut
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    
    // Numéro de téléphone (pour mobile money)
    private String phoneNumber;
    
    // Dates
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    // Erreurs
    private String errorMessage;
    private String errorCode;
    
    // Facture
    private String invoiceUrl;
    private Boolean invoiceSent;
}
