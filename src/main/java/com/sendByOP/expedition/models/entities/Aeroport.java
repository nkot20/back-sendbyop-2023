package com.sendByOP.expedition.models.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@Entity
@Table(name = "aeroport")
public class Aeroport implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idaero")
    private Integer idaero;

    @Column(name = "nom")
    private String nom;

    @JoinColumn(name = "id_ville", referencedColumnName = "idville")
    @ManyToOne
    private Ville idVille;

    /*@OneToMany(cascade = CascadeType.ALL, mappedBy = "idaeroDepart")
    private List<Vol> aeroPortsDepart;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idAeroArrive")
    private List<Vol> aeroportsArrive;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "aeroport")
    private List<Escale> escaleList;*/


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idaero != null ? idaero.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Aeroport)) {
            return false;
        }
        Aeroport other = (Aeroport) object;
        if ((this.idaero == null && other.idaero != null) || (this.idaero != null && !this.idaero.equals(other.idaero))) {
            return false;
        }
        return true;
    }

}
