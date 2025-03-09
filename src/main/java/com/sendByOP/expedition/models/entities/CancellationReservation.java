package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cancellation_reservation")
public class CancellationReservation extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancellation_id", nullable = false)
    private Integer cancellationId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "cancellation_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date cancellationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reservation_id", referencedColumnName = "reservation_id")
    private Booking reservation;

    @Column(name = "viewed")
    private Boolean viewed;
}
