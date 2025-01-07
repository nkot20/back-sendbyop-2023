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
@Table(name = "operation")
public class Operation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_ope")
    private Integer idOpe;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @JoinColumn(name = "id_type_operation", referencedColumnName = "id_type_operation")
    @ManyToOne(optional = false)
    private Typeoperation idTypeOperation;

    @JoinColumn(name = "id_reser", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Reservation idReser;


    public Operation() {
    }

    public Operation(Integer idOpe) {
        this.idOpe = idOpe;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOpe != null ? idOpe.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Operation)) {
            return false;
        }
        Operation other = (Operation) object;
        if ((this.idOpe == null && other.idOpe != null) || (this.idOpe != null && !this.idOpe.equals(other.idOpe))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Operation[ idOpe=" + idOpe + " ]";
    }

}
