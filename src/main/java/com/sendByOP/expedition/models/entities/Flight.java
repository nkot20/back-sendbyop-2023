package com.sendByOP.expedition.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "flight")
public class Flight extends BaseEntity implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Basic(optional = false)
        @Column(name = "id")
        private Integer flightId;

        @Column(name = "arrival_date")
        @Temporal(TemporalType.TIMESTAMP)
        private Date arrivalDate;

        @Column(name = "departure_date")
        @Temporal(TemporalType.TIMESTAMP)
        private Date departureDate;

        @Column(name = "arrival_time")
        private String arrivalTime;

        @Column(name = "departure_time")
        private String departureTime;

        @Column(name = "amount_per_kg")
        private Integer amountPerKg;

        @Column(name = "kg_count")
        private Integer kgCount;

        @JoinColumn(name = "departure_airport_id", referencedColumnName = "airport_id")
        @ManyToOne(optional = false)
        private Airport departureAirport;

        @JoinColumn(name = "arrival_airport_id", referencedColumnName = "airport_id")
        @ManyToOne(optional = false)
        private Airport arrivalAirport;

        @JoinColumn(name = "customer_id", referencedColumnName = "id")
        @ManyToOne
        @JsonIgnoreProperties(value = {"applications", "hibernateLazyInitializer"})
        private Customer customer;

        @Basic(optional = false)
        @Column(name = "validation_status")
        private int validationStatus;

        @Column(name = "preference")
        private String preference;

        @Column(name = "publication_date")
        @Temporal(TemporalType.TIMESTAMP)
        private Date publicationDate;

        @Column(name = "image")
        private String image;

        @Column(name = "deposit_location")
        private String depositLocation;

        @Column(name = "reception_location")
        private String receptionLocation;

        @Column(name = "cancelled")
        private int cancelled;

}
