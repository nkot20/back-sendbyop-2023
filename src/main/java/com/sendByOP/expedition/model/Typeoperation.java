package com.sendByOP.expedition.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "typeoperation")
public class Typeoperation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_type_operation")
    private Integer idtypeoperation;
    @Basic(optional = false)
    @Column(name = "intitule")
    private String intitule;


    public Typeoperation() {
    }

    public Typeoperation(Integer idTypeOperation) {
        this.idtypeoperation = idTypeOperation;
    }

    public Typeoperation(Integer idTypeOperation, String intitule) {
        this.idtypeoperation = idTypeOperation;
        this.intitule = intitule;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idtypeoperation != null ? idtypeoperation.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Typeoperation)) {
            return false;
        }
        Typeoperation other = (Typeoperation) object;
        if ((this.idtypeoperation == null && other.idtypeoperation != null) || (this.idtypeoperation != null && !this.idtypeoperation.equals(other.idtypeoperation))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Typeoperation[ idTypeOperation=" + idtypeoperation + " ]";
    }

}
