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
@Table(name = "payment")
@NoArgsConstructor
public class Payment extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "payment_id")
    private Integer paymentId;

    @Basic(optional = false)
    @Column(name = "payment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer client;

    @Column(name = "amount")
    private Double amount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_type_id", referencedColumnName = "payment_type_id")
    private PaymentType paymentType;
}
