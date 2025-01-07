package com.sendByOP.expedition.models.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "escale")
public class Escale implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_escale")
    private Integer idEscale;

    @Column(name = "datees")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datees;

    @Column(name = "heure")
    private String heure;

    @JoinColumn(name = "idaero", referencedColumnName = "idaero")
    @ManyToOne(optional = false)
    private Aeroport idaero;

    @JoinColumn(name = "idvol", referencedColumnName = "idvol")
    @ManyToOne(optional = false)
    private Vol idvol;

    public Escale() {
    }


    public void setIdvol(Vol idvol) {
        this.idvol = idvol;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEscale != null ? idEscale.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Escale)) {
            return false;
        }
        Escale other = (Escale) object;
        if ((this.idEscale == null && other.idEscale != null) || (this.idEscale != null && !this.idEscale.equals(other.idEscale))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Escale[ idEscale=" + idEscale + " ]";
    }
}
