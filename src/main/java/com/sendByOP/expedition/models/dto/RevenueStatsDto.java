package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour les statistiques de revenus
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsDto {
    
    /**
     * Revenu total (toutes réservations payées)
     */
    private BigDecimal totalRevenue;
    
    /**
     * Revenu en attente (réservations confirmées non payées)
     */
    private BigDecimal pendingRevenue;
    
    /**
     * Commission plateforme totale
     */
    private BigDecimal totalCommission;
    
    /**
     * Montant total versé aux voyageurs
     */
    private BigDecimal totalPaidToTravelers;
    
    /**
     * Montant en attente de versement aux voyageurs
     */
    private BigDecimal pendingPayoutToTravelers;
    
    /**
     * Revenu moyen par réservation
     */
    private BigDecimal averageRevenuePerBooking;
    
    /**
     * Nombre de réservations payées
     */
    private Long paidBookingsCount;
    
    /**
     * Taux de commission moyen (en pourcentage)
     */
    private Double averageCommissionRate;
    
    /**
     * Revenu perdu (réservations annulées après paiement)
     */
    private BigDecimal lostRevenue;
}
