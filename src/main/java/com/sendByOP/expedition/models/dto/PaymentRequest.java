package com.sendByOP.expedition.models.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO pour le traitement d'un paiement
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    @NotNull(message = "Le montant du paiement est requis")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;
    
    @NotBlank(message = "La méthode de paiement est requise")
    private String paymentMethod; // CREDIT_CARD, PAYPAL, STRIPE, etc.
    
    // Informations carte de crédit (si applicable)
    private String cardNumber;
    private String cardHolderName;
    private String cardExpiryMonth;
    private String cardExpiryYear;
    private String cardCvv;
    
    // Token de paiement (Stripe, PayPal, etc.)
    private String paymentToken;
    
    // ID de transaction externe
    private String externalTransactionId;
    
    // Notes additionnelles
    private String notes;
}
