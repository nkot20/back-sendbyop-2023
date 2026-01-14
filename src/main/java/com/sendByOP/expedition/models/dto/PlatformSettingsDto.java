package com.sendByOP.expedition.models.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les paramètres de configuration de la plateforme
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PlatformSettingsDto implements Serializable {
    
    private Integer id;
    
    // ==========================================
    // TARIFS
    // ==========================================
    
    @NotNull(message = "Le prix minimum par kg est requis")
    @DecimalMin(value = "0.01", message = "Le prix minimum doit être supérieur à 0")
    private BigDecimal minPricePerKg;
    
    @NotNull(message = "Le prix maximum par kg est requis")
    @DecimalMin(value = "0.01", message = "Le prix maximum doit être supérieur à 0")
    private BigDecimal maxPricePerKg;
    
    // ==========================================
    // RÉPARTITION (pourcentages)
    // ==========================================
    
    @NotNull(message = "Le pourcentage voyageur est requis")
    @DecimalMin(value = "0", message = "Le pourcentage voyageur doit être positif")
    @DecimalMax(value = "100", message = "Le pourcentage voyageur ne peut dépasser 100")
    private BigDecimal travelerPercentage;
    
    @NotNull(message = "Le pourcentage plateforme est requis")
    @DecimalMin(value = "0", message = "Le pourcentage plateforme doit être positif")
    @DecimalMax(value = "100", message = "Le pourcentage plateforme ne peut dépasser 100")
    private BigDecimal platformPercentage;
    
    @NotNull(message = "Le pourcentage TVA est requis")
    @DecimalMin(value = "0", message = "Le pourcentage TVA doit être positif")
    @DecimalMax(value = "100", message = "Le pourcentage TVA ne peut dépasser 100")
    private BigDecimal vatPercentage;
    
    // ==========================================
    // DÉLAIS (heures)
    // ==========================================
    
    @NotNull(message = "Le délai de paiement est requis")
    @Min(value = 2, message = "Le délai de paiement minimum est de 2 heures")
    @Max(value = 24, message = "Le délai de paiement maximum est de 24 heures")
    private Integer paymentTimeoutHours;
    
    @NotNull(message = "Le délai de versement est requis")
    @Min(value = 12, message = "Le délai de versement minimum est de 12 heures")
    @Max(value = 72, message = "Le délai de versement maximum est de 72 heures")
    private Integer autoPayoutDelayHours;
    
    @NotNull(message = "Le délai d'annulation est requis")
    @Min(value = 12, message = "Le délai d'annulation minimum est de 12 heures")
    @Max(value = 72, message = "Le délai d'annulation maximum est de 72 heures")
    private Integer cancellationDeadlineHours;
    
    // ==========================================
    // PÉNALITÉS
    // ==========================================
    
    @NotNull(message = "La pénalité d'annulation tardive est requise")
    @DecimalMin(value = "0", message = "La pénalité doit être positive")
    @DecimalMax(value = "1", message = "La pénalité ne peut dépasser 100%")
    private BigDecimal lateCancellationPenalty;
    
    // ==========================================
    // COMMISSIONS ET FRAIS
    // ==========================================
    
    @NotNull(message = "La commission plateforme est requise")
    @DecimalMin(value = "0", message = "La commission doit être positive")
    @DecimalMax(value = "100", message = "La commission ne peut dépasser 100%")
    private BigDecimal commissionPercentage;
    
    @NotNull(message = "Le montant minimum de reversement est requis")
    @DecimalMin(value = "0", message = "Le montant doit être positif")
    private BigDecimal minimumPayoutAmount;
    
    @NotNull(message = "Les frais couverts sont requis")
    @DecimalMin(value = "0", message = "Les frais doivent être positifs")
    private BigDecimal transferFeeCovered;
    
    @NotNull(message = "Le montant d'assurance est requis")
    @DecimalMin(value = "0", message = "Le montant d'assurance doit être positif")
    private BigDecimal insuranceAmount;
    
    @NotNull(message = "Le taux de TVA Europe est requis")
    @DecimalMin(value = "0", message = "Le taux de TVA doit être positif")
    @DecimalMax(value = "100", message = "Le taux de TVA ne peut dépasser 100%")
    private BigDecimal vatRateEurope;
    
    // ==========================================
    // DÉLAIS SUPPLÉMENTAIRES
    // ==========================================
    
    @NotNull(message = "Le délai de confirmation de réception est requis")
    @Min(value = 24, message = "Le délai minimum est de 24 heures")
    @Max(value = 168, message = "Le délai maximum est de 168 heures")
    private Integer receptionConfirmationHours;
    
    @NotNull(message = "Le délai pour donner un avis est requis")
    @Min(value = 30, message = "Le délai minimum est de 30 jours")
    @Max(value = 365, message = "Le délai maximum est de 365 jours")
    private Integer reviewDeadlineDays;
    
    @NotNull(message = "Le délai critique d'annulation est requis")
    @Min(value = 1, message = "Le délai minimum est de 1 heure")
    @Max(value = 24, message = "Le délai maximum est de 24 heures")
    private Integer criticalCancellationHours;
    
    // ==========================================
    // REMBOURSEMENTS
    // ==========================================
    
    @NotNull(message = "Le taux de remboursement est requis")
    @DecimalMin(value = "0", message = "Le taux doit être positif")
    @DecimalMax(value = "100", message = "Le taux ne peut dépasser 100%")
    private BigDecimal refundRateBeforeDeadline;
    
    // ==========================================
    // AUDIT
    // ==========================================
    
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    // ==========================================
    // MÉTHODES DE VALIDATION
    // ==========================================
    
    /**
     * Vérifie que la somme des pourcentages = 100%
     */
    @AssertTrue(message = "La somme des pourcentages doit être égale à 100%")
    public boolean isPercentageSumValid() {
        if (travelerPercentage == null || platformPercentage == null || vatPercentage == null) {
            return false;
        }
        BigDecimal sum = travelerPercentage.add(platformPercentage).add(vatPercentage);
        return sum.compareTo(BigDecimal.valueOf(100)) == 0;
    }
    
    /**
     * Vérifie que minPrice < maxPrice
     */
    @AssertTrue(message = "Le prix minimum doit être inférieur au prix maximum")
    public boolean isPriceRangeValid() {
        if (minPricePerKg == null || maxPricePerKg == null) {
            return false;
        }
        return minPricePerKg.compareTo(maxPricePerKg) < 0;
    }
}
