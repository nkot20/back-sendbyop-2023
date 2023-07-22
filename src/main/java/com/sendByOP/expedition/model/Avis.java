package com.sendByOP.expedition.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@Table(name = "avis")
public class Avis implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id_avis")
    private int idavis;

    @Column(name = "mention")
    private String mention;

    @Column(name = "opinion")
    private String opinion;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    /*@JoinColumn(name = "reservation", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Reservation reservation;*/

    /*@JoinColumn(name = "expediteur", referencedColumnName = "idp")
    @ManyToOne(optional = false)
    private Client expediteur;*/

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "transporteur", referencedColumnName = "idp")
    @ManyToOne(optional = false)
    private Client transporteur;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "expediteur", referencedColumnName = "idp")
    @ManyToOne(optional = false)
    private Client expediteur;


}


