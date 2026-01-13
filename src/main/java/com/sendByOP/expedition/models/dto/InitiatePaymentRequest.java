package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {
    
    @NotNull(message = "L'ID de la réservation est requis")
    private Integer bookingId;
    
    @NotNull(message = "Le montant est requis")
    @DecimalMin(value = "100.0", message = "Le montant minimum est de 100 FCFA")
    private BigDecimal amount;
    
    @NotNull(message = "La méthode de paiement est requise")
    private PaymentMethod paymentMethod;
    
    // Pour Orange Money et MTN Mobile Money
    @Pattern(regexp = "^(\\+237)?[0-9]{9}$", message = "Numéro de téléphone invalide")
    private String phoneNumber;
    
    // Pour carte bancaire
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    
    // Pour PayPal
    private String paypalEmail;
    
    @NotBlank(message = "L'email du client est requis")
    @Email(message = "Email invalide")
    private String customerEmail;
    
    private String customerName;
}
