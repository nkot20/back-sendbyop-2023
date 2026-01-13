package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPaymentNotification {
    
    private String transactionReference;
    private String externalTransactionId;
    private String status; // SUCCESS, FAILED, PENDING
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String phoneNumber;
    private LocalDateTime timestamp;
    private String signature; // Pour vérifier l'authenticité du webhook
    private String message;
    private String errorCode;
}
