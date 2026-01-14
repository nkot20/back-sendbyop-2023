package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.enums.RecipientStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entité représentant un destinataire de colis
 * Un destinataire est identifié de manière unique par son email OU son numéro de téléphone
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "receiver", uniqueConstraints = {
    @UniqueConstraint(name = "uk_receiver_email", columnNames = "email"),
    @UniqueConstraint(name = "uk_receiver_phone", columnNames = "phone_number")
})
public class Receiver extends BaseEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "country")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecipientStatus status = RecipientStatus.ACTIVE;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = RecipientStatus.ACTIVE;
        }
    }
}
