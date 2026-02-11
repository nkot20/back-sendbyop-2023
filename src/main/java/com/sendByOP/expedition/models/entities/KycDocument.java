package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Entité pour stocker les documents KYC des clients
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "kyc_document")
@NoArgsConstructor
@AllArgsConstructor
public class KycDocument extends BaseEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "document_type", nullable = false)
    private String documentType; // PASSPORT, ID_CARD, DRIVER_LICENSE
    
    @Column(name = "document_number")
    private String documentNumber;
    
    @Column(name = "front_image_path")
    private String frontImagePath;
    
    @Column(name = "back_image_path")
    private String backImagePath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KycStatus status = KycStatus.PENDING_REVIEW;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "submitted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedAt;
    
    @Column(name = "reviewed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewedAt;
    
    @Column(name = "reviewed_by")
    private String reviewedBy; // Email de l'admin qui a validé/rejeté
    
    @Column(name = "expiry_date")
    @Temporal(TemporalType.DATE)
    private Date expiryDate;
    
    @Column(name = "country_of_issue")
    private String countryOfIssue;
    
    /**
     * Vérifie si le document est expiré
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.before(new Date());
    }
    
    /**
     * Vérifie si le document est approuvé et non expiré
     */
    public boolean isValid() {
        return status == KycStatus.APPROVED && !isExpired();
    }
}
