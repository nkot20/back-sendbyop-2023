package com.sendByOP.expedition.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Entité représentant une photo d'un colis
 * Permet d'avoir plusieurs photos par réservation pour une meilleure transparence
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parcel_photo", indexes = {
    @Index(name = "idx_photo_booking", columnList = "booking_id")
})
public class ParcelPhoto extends BaseEntity implements Serializable {

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
     * Indique si c'est la photo principale
     */
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    /**
     * Réservation associée
     */
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", referencedColumnName = "id", nullable = false)
    private Booking booking;
}
