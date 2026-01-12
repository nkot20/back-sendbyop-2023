package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un versement au voyageur
 * Gère la répartition des montants selon les paramètres de la plateforme
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "payout", indexes = {
    @Index(name = "idx_payout_booking", columnList = "booking_id"),
    @Index(name = "idx_payout_traveler", columnList = "traveler_id"),
    @Index(name = "idx_payout_status", columnList = "status"),
    @Index(name = "idx_payout_created_at", columnList = "created_at")
})
public class Payout implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id", nullable = false)
    private Customer traveler;
    
    // Montants
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "traveler_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal travelerAmount;
    
    @Column(name = "platform_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformAmount;
    
    @Column(name = "vat_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal vatAmount;
    
    // Pourcentages appliqués
    @Column(name = "traveler_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal travelerPercentage;
    
    @Column(name = "platform_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal platformPercentage;
    
    @Column(name = "vat_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatPercentage;
    
    // Statut et traçabilité
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayoutStatus status = PayoutStatus.PENDING;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    // Dates
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PayoutStatus.PENDING;
        }
    }
    
    /**
     * Marque le versement comme complété
     */
    public void markAsCompleted(String transactionId) {
        this.status = PayoutStatus.COMPLETED;
        this.transactionId = transactionId;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = null;
    }
    
    /**
     * Marque le versement comme échoué
     */
    public void markAsFailed(String errorMessage) {
        this.status = PayoutStatus.FAILED;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Marque le versement comme annulé
     */
    public void markAsCancelled(String reason) {
        this.status = PayoutStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.errorMessage = reason;
    }
    
    /**
     * Vérifie que la répartition est correcte
     */
    public boolean validateAmounts() {
        BigDecimal calculatedSum = travelerAmount.add(platformAmount).add(vatAmount);
        return calculatedSum.compareTo(totalAmount) == 0;
    }
}
