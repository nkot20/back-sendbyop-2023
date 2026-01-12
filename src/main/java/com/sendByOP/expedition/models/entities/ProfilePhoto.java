package com.sendByOP.expedition.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Entité représentant une photo de profil d'un client
 * Permet d'avoir plusieurs photos de profil (galerie)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profile_photo", indexes = {
    @Index(name = "idx_profile_photo_customer", columnList = "customer_id")
})
public class ProfilePhoto extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * URL de la photo stockée
     */
    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    /**
     * Description optionnelle de la photo
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Ordre d'affichage (pour trier les photos)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Indique si c'est la photo de profil principale
     */
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    /**
     * Client associé
     */
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;
}
