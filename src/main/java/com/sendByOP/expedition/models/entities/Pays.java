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
@Table(name = "pays")
public class Pays implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idpays")
    private Integer idpays;

    @Column(name = "nom")
    private String nom;


    public Pays() {
    }

    public Pays(Integer idpays) {
        this.idpays = idpays;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idpays != null ? idpays.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pays)) {
            return false;
        }
        Pays other = (Pays) object;
        if ((this.idpays == null && other.idpays != null) || (this.idpays != null && !this.idpays.equals(other.idpays))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Pays[ idpays=" + idpays + " ]";
    }

}
