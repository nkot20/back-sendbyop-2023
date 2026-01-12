package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Entité représentant une réservation de transport de colis
 * Gère le cycle de vie complet d'une réservation
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "booking", indexes = {
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_customer", columnList = "customer_id"),
    @Index(name = "idx_booking_flight", columnList = "flight_id"),
    @Index(name = "idx_booking_date", columnList = "booking_date")
})
public class Booking extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    // ==========================================
    // STATUT (Nouveau système avec enum)
    // ==========================================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status = BookingStatus.PENDING_CONFIRMATION;

    // ==========================================
    // RELATIONS
    // ==========================================
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private Receiver receiver;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", referencedColumnName = "id", nullable = false)
    private Flight flight;
    
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Parcel> parcels;

    // ==========================================
    // DATES ET TIMESTAMPS
    // ==========================================
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "booking_date", nullable = false)
    private Date bookingDate = new Date();

    @Column(name = "booking_time")
    private String bookingTime;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ==========================================
    // PHOTOS DU COLIS (Support multi-photos)
    // ==========================================
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ParcelPhoto> parcelPhotos;
    
    /**
     * @deprecated Utiliser parcelPhotos à la place. Conservé pour compatibilité.
     */
    @Deprecated
    @Column(name = "parcel_photo_url")
    private String parcelPhotoUrl;

    // ==========================================
    // MONTANTS
    // ==========================================
    
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    // ==========================================
    // ANNULATION
    // ==========================================
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // ==========================================
    // ANCIENS CHAMPS (Compatibilité backward)
    // ==========================================
    
    @Column(name = "payment_status")
    private Integer paymentStatus;

    @Column(name = "expedition_status")
    private Integer expeditionStatus;

    @Column(name = "customer_review")
    private String customerReview;

    @Column(name = "sender_review")
    private String senderReview;

    @Column(name = "sender_reception_status")
    private int senderReceptionStatus;

    @Column(name = "customer_reception_status")
    private int customerReceptionStatus;

    @Column(name = "cancelled")
    private int cancelled;

    @Column(name = "transporter_payment_status")
    private int transporterPaymentStatus;
    
    // ==========================================
    // MÉTHODES UTILITAIRES
    // ==========================================
    
    @PrePersist
    protected void onCreate() {
        if (bookingDate == null) {
            bookingDate = new Date();
        }
        if (status == null) {
            status = BookingStatus.PENDING_CONFIRMATION;
        }
    }
    
    /**
     * Vérifie si la réservation peut être annulée
     */
    public boolean canBeCancelled() {
        return status != null && status.canBeCancelledByClient();
    }
    
    /**
     * Vérifie si la réservation est active
     */
    public boolean isActive() {
        return status != null && status.isActive();
    }
    
    /**
     * Vérifie si le paiement est en attente
     */
    public boolean isPaymentPending() {
        return status == BookingStatus.CONFIRMED_UNPAID;
    }
    
    /**
     * Vérifie si le délai de paiement est dépassé
     */
    public boolean isPaymentDeadlineExpired() {
        return paymentDeadline != null && LocalDateTime.now().isAfter(paymentDeadline);
    }
}
