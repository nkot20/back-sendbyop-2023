package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Log des notifications envoyées
 * Permet la traçabilité de toutes les communications
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notification_log", indexes = {
    @Index(name = "idx_notification_booking", columnList = "booking_id"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_sent_at", columnList = "sent_at")
})
public class NotificationLog extends BaseEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    
    @Column(name = "recipient_name")
    private String recipientName;
    
    @Column(name = "subject", nullable = false)
    private String subject;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "sent", nullable = false)
    private Boolean sent = false;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sent == null) {
            sent = false;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
    
    /**
     * Marque la notification comme envoyée
     */
    public void markAsSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
        this.errorMessage = null;
    }
    
    /**
     * Marque la notification comme échouée
     */
    public void markAsFailed(String errorMessage) {
        this.sent = false;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }
}
