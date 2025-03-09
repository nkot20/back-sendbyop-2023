package com.sendByOP.expedition.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@Entity
@Table(name = "parcel")
public class Parcel implements Serializable {

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parcel_id")
    private Integer id;

    @Column(name = "description")
    private String description;

    @Column(name = "weight_kg")
    private Float weightKg;

    @Column(name = "parcel_type")
    private String parcelType;

    @JsonIgnore
    @JoinColumn(name = "reservation_id", referencedColumnName = "reservation_id")
    @ManyToOne(optional = false)
    private Booking reservation;
}
