package com.sendByOP.expedition.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "facture")
public class Facture implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idfac")
    private Integer idfac;

    @Column(name = "montantfac")
    private Float montantfac;

    @Column(name = "datepaie")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datepaie;

    @JoinColumn(name = "id_re", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Reservation idRe;

    public Facture() {
    }

    public Facture(Integer idfac) {
        this.idfac = idfac;
    }

    public Integer getIdfac() {
        return idfac;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idfac != null ? idfac.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Facture)) {
            return false;
        }
        Facture other = (Facture) object;
        if ((this.idfac == null && other.idfac != null) || (this.idfac != null && !this.idfac.equals(other.idfac))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Facture[ idfac=" + idfac + " ]";
    }

}
