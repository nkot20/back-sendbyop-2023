package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "rating")
@NoArgsConstructor
public class Rating extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "rating_id")
    private Integer id;

    @Basic(optional = false)
    @Column(name = "score")
    private int score;

    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Customer sender;

    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Customer customer;
}
