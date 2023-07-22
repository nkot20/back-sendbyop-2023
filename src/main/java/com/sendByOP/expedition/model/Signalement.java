package com.sendByOP.expedition.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "signalement")
public class Signalement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_s")
    private Integer idS;
    @Basic(optional = false)
    @Column(name = "id_vol")
    private int idVol;
    @Basic(optional = false)
    @Column(name = "motif")
    private String motif;
    @Basic(optional = false)
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    public Signalement() {
    }

    public Signalement(Integer idS) {
        this.idS = idS;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idS != null ? idS.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Signalement)) {
            return false;
        }
        Signalement other = (Signalement) object;
        if ((this.idS == null && other.idS != null) || (this.idS != null && !this.idS.equals(other.idS))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Signalement[ idS=" + idS + " ]";
    }

}
