package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.PayoutStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les paiements aux voyageurs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutDto {
    
    /**
     * ID du payout
     */
    private Integer id;
    
    /**
     * ID de la réservation associée
     */
    private Integer bookingId;
    
    /**
     * ID du voyageur (recipient du payout)
     */
    private Integer travelerId;
    
    /**
     * Email du voyageur
     */
    private String travelerEmail;
    
    /**
     * Nom complet du voyageur
     */
    private String travelerName;
    
    /**
     * Montant total de la réservation
     */
    private BigDecimal bookingAmount;
    
    /**
     * Commission de la plateforme
     */
    private BigDecimal platformCommission;
    
    /**
     * Taux de commission (en pourcentage)
     */
    private Double commissionRate;
    
    /**
     * Montant net versé au voyageur
     */
    private BigDecimal payoutAmount;
    
    /**
     * Statut du payout
     */
    private PayoutStatus status;
    
    /**
     * Date de création
     */
    private LocalDateTime createdAt;
    
    /**
     * Date de traitement (paiement effectué)
     */
    private LocalDateTime processedAt;
    
    /**
     * Méthode de paiement
     */
    private String paymentMethod;
    
    /**
     * ID de transaction externe (si applicable)
     */
    private String externalTransactionId;
    
    /**
     * Notes ou commentaires
     */
    private String notes;
}
