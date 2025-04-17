package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "invoice")
@NoArgsConstructor
public class Invoice implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "invoice_id")
    private Integer id;

    @Column(name = "amount")
    private Float amount;

    @Column(name = "payment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    @JoinColumn(name = "reservation_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Booking reservation;

}
