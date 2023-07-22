package com.sendByOP.expedition.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "type_virement")
public class TypeVirement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_type")
    private Integer idType;
    @Basic(optional = false)
    @Column(name = "intitule")
    private String intitule;


    public TypeVirement() {
    }

    public TypeVirement(Integer idType) {
        this.idType = idType;
    }

    public TypeVirement(Integer idType, String intitule) {
        this.idType = idType;
        this.intitule = intitule;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idType != null ? idType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TypeVirement)) {
            return false;
        }
        TypeVirement other = (TypeVirement) object;
        if ((this.idType == null && other.idType != null) || (this.idType != null && !this.idType.equals(other.idType))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.TypeVirement[ idType=" + idType + " ]";
    }

}
