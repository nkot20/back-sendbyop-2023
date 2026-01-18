package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.enums.PaymentMethod;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "transaction_reference", unique = true, nullable = false, length = 100)
    private String transactionReference;
    
    @Column(name = "external_transaction_id", length = 255)
    private String externalTransactionId; // ID de la transaction chez le provider
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Builder.Default
    @Column(name = "currency", length = 10, nullable = false)
    private String currency = "XAF"; // Franc CFA
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // Pour Orange Money et MTN
    
    @Column(name = "payment_details", columnDefinition = "TEXT")
    private String paymentDetails; // JSON avec détails supplémentaires
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "error_code", length = 50)
    private String errorCode;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "webhook_received_at")
    private LocalDateTime webhookReceivedAt;
    
    @Column(name = "invoice_url", length = 500)
    private String invoiceUrl; // Lien vers la facture PDF
    
    @Builder.Default
    @Column(name = "invoice_sent", nullable = false)
    private Boolean invoiceSent = false;
    
    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey; // Pour éviter les doubles paiements
    
    @PrePersist
    protected void onCreate() {
        if (transactionReference == null) {
            transactionReference = generateTransactionReference();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (status == TransactionStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    private String generateTransactionReference() {
        return "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
}
