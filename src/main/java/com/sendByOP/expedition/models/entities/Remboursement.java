package com.sendByOP.expedition.models.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "remboursement")
public class Remboursement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_rem")
    private Integer idRem;

    @JoinColumn(name = "id_reservation", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Reservation reservation;

    public Remboursement() {
    }

    public Remboursement(Integer idRem) {
        this.idRem = idRem;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idRem != null ? idRem.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Remboursement)) {
            return false;
        }
        Remboursement other = (Remboursement) object;
        if ((this.idRem == null && other.idRem != null) || (this.idRem != null && !this.idRem.equals(other.idRem))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Remboursement[ idRem=" + idRem + " ]";
    }

}
