package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques de paiement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsDto {
    private Integer totalPayments;
    private Double totalAmount;
    private Double averageAmount;
    private Integer pendingPayments;
    private Integer completedPayments;
}
