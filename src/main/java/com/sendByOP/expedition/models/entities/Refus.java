package com.sendByOP.expedition.models.entities;

import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@AllArgsConstructor
@Entity
@Table(name = "refus")
public class Refus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idre")
    private Integer idre;

    @Column(name = "motif")
    private String motif;

    @JoinColumn(name = "id_re", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Reservation idRe;

    public Refus() {
    }

    public Refus(Integer idre) {
        this.idre = idre;
    }

    public Integer getIdre() {
        return idre;
    }

    public void setIdre(Integer idre) {
        this.idre = idre;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Reservation getIdRe() {
        return idRe;
    }

    public void setIdRe(Reservation idRe) {
        this.idRe = idRe;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idre != null ? idre.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Refus)) {
            return false;
        }
        Refus other = (Refus) object;
        if ((this.idre == null && other.idre != null) || (this.idre != null && !this.idre.equals(other.idre))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Refus[ idre=" + idre + " ]";
    }


}
