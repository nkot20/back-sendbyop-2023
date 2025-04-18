package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stopover")
public class Stopover extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "stopover_id")
    private Integer id;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "hour")
    private String hour;

    @JoinColumn(name = "airport_id", referencedColumnName = "airport_id")
    @ManyToOne(optional = false)
    private Airport airport;

    @JoinColumn(name = "flight_id", referencedColumnName = "flight_id")
    @ManyToOne(optional = false)
    private Flight flight;
}
