package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;


@AllArgsConstructor
@Getter
@ToString
@Entity
@Table(name = "annulation_trajet")
@NoArgsConstructor
@Builder
public class CancellationTrip extends BaseEntity implements Serializable {
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
    @JoinColumn(name = "trip_id", referencedColumnName = "id")
    private Flight trip;

    @Column(name = "viewed")
    private Boolean viewed;

}
