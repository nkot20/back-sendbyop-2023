package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.PayoutStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les versements aux voyageurs dans le panel d'administration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPayoutDto {
    
    private Long id;
    
    // Informations de réservation
    private Integer bookingId;
    private String bookingReference;
    
    // Informations voyageur
    private Integer travelerId;
    private String travelerName;
    private String travelerEmail;
    
    // Montants
    private BigDecimal totalAmount;
    private BigDecimal travelerAmount;
    private BigDecimal platformAmount;
    private BigDecimal vatAmount;
    
    // Pourcentages
    private BigDecimal travelerPercentage;
    private BigDecimal platformPercentage;
    private BigDecimal vatPercentage;
    
    // Statut et traçabilité
    private PayoutStatus status;
    private String transactionId;
    private String paymentMethod;
    private String errorMessage;
    
    // Dates
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}
