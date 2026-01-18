package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.PaymentMethod;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Integer transactionId;
    private String transactionReference;
    private Integer bookingId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private String statusMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    // Pour les paiements nécessitant une confirmation externe
    private String paymentUrl; // URL de redirection pour PayPal ou paiement carte
    private String qrCodeData; // Données QR code pour Orange Money / MTN
    private String ussdCode; // Code USSD pour validation
    
    private boolean requiresConfirmation;
    private String confirmationMessage;
}
