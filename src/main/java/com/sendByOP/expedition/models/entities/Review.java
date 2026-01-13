package com.sendByOP.expedition.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@Table(name = "review")
public class Review extends BaseEntity implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "review_id")
    private int id;

    @Column(name = "rating")
    private String rating;

    @Column(name = "opinion")
    private String opinion;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "transporter_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Customer transporter;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "shipper_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Customer shipper;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "booking_id", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private Booking booking;

    @Column(name = "response")
    private String response;

    @Column(name = "response_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date responseDate;

}


