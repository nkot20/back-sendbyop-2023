package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "booking")
public class Booking extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "booking_date", nullable = false)
    private Date bookingDate = new Date();

    @Column(name = "booking_time")
    private String bookingTime;

    @Column(name = "payment_status")
    private Integer paymentStatus;

    @Column(name = "expedition_status")
    private Integer expeditionStatus;

    @Column(name = "customer_review")
    private String customerReview;

    @Column(name = "sender_review")
    private String senderReview;

    @Column(name = "sender_reception_status")
    private int senderReceptionStatus;

    @Column(name = "customer_reception_status")
    private int customerReceptionStatus;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private Receiver receiver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flight_id", referencedColumnName = "id")
    private Flight flight;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column(name = "cancelled")
    private int cancelled;

    @Column(name = "transporter_payment_status")
    private int transporterPaymentStatus;

}
