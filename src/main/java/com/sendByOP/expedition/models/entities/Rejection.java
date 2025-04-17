package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "rejection")
public class Rejection extends BaseEntity implements Serializable {@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rejection_id", nullable = false)
    private Integer id;

    @Column(name = "reason", nullable = false)
    private String reason;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reservation_id", referencedColumnName = "id")
    private Booking reservation;
}
