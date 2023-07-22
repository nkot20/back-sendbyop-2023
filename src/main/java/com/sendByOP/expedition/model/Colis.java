package com.sendByOP.expedition.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Entity
@Table(name = "colis")
public class Colis implements Serializable {

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcol")
    private Integer idcol;

    @Column(name = "description")
    private String description;

    @Column(name = "kilo")
    private Float kilo;


    @Column(name = "type_colis")
    private String typeColis;

    @JsonIgnore
    @JoinColumn(name = "id_re", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Reservation idre;


}
