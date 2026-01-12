package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Paramètres de configuration de la plateforme
 * Configuration centralisée des tarifs, répartitions et délais
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "platform_settings")
public class PlatformSettings implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    
    // ==========================================
    // TARIFS
    // ==========================================
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Le prix minimum par kg doit être supérieur à 0")
    @Column(name = "min_price_per_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal minPricePerKg = BigDecimal.valueOf(5.00);
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Le prix maximum par kg doit être supérieur à 0")
    @Column(name = "max_price_per_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxPricePerKg = BigDecimal.valueOf(50.00);
    
    // ==========================================
    // RÉPARTITION (en pourcentage)
    // ==========================================
    
    @NotNull
    @DecimalMin(value = "0", message = "Le pourcentage voyageur doit être positif")
    @DecimalMax(value = "100", message = "Le pourcentage voyageur ne peut pas dépasser 100")
    @Column(name = "traveler_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal travelerPercentage = BigDecimal.valueOf(70.00);
    
    @NotNull
    @DecimalMin(value = "0", message = "Le pourcentage plateforme doit être positif")
    @DecimalMax(value = "100", message = "Le pourcentage plateforme ne peut pas dépasser 100")
    @Column(name = "platform_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal platformPercentage = BigDecimal.valueOf(25.00);
    
    @NotNull
    @DecimalMin(value = "0", message = "Le pourcentage TVA doit être positif")
    @DecimalMax(value = "100", message = "Le pourcentage TVA ne peut pas dépasser 100")
    @Column(name = "vat_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatPercentage = BigDecimal.valueOf(5.00);
    
    // ==========================================
    // DÉLAIS (en heures)
    // ==========================================
    
    @NotNull
    @Min(value = 2, message = "Le délai de paiement minimum est de 2 heures")
    @Max(value = 24, message = "Le délai de paiement maximum est de 24 heures")
    @Column(name = "payment_timeout_hours", nullable = false)
    private Integer paymentTimeoutHours = 12;
    
    @NotNull
    @Min(value = 12, message = "Le délai de versement minimum est de 12 heures")
    @Max(value = 72, message = "Le délai de versement maximum est de 72 heures")
    @Column(name = "auto_payout_delay_hours", nullable = false)
    private Integer autoPayoutDelayHours = 24;
    
    @NotNull
    @Min(value = 12, message = "Le délai d'annulation minimum est de 12 heures")
    @Max(value = 72, message = "Le délai d'annulation maximum est de 72 heures")
    @Column(name = "cancellation_deadline_hours", nullable = false)
    private Integer cancellationDeadlineHours = 24;
    
    // ==========================================
    // PÉNALITÉS
    // ==========================================
    
    @NotNull
    @DecimalMin(value = "0", message = "La pénalité doit être positive")
    @DecimalMax(value = "1", message = "La pénalité ne peut pas dépasser 100%")
    @Column(name = "late_cancellation_penalty", nullable = false, precision = 5, scale = 2)
    private BigDecimal lateCancellationPenalty = BigDecimal.valueOf(0.50); // 50%
    
    // ==========================================
    // AUDIT
    // ==========================================
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Valide que la somme des pourcentages est égale à 100%
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
     * Valide que le prix minimum est inférieur au prix maximum
     */
    @AssertTrue(message = "Le prix minimum doit être inférieur au prix maximum")
    public boolean isPriceRangeValid() {
        if (minPricePerKg == null || maxPricePerKg == null) {
            return false;
        }
        return minPricePerKg.compareTo(maxPricePerKg) < 0;
    }
}
