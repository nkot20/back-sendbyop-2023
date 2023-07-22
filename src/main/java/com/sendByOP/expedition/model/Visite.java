package com.sendByOP.expedition.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "visites")
public class Visite implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Basic(optional = false)
    @Column(name = "date_v")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateV;

    @Column(name = "adresse_ip")
    private String ipadress;

    public Visite() {
    }

    public Visite(Integer id) {
        this.id = id;
    }

    public Visite(Integer id, Date dateV) {
        this.id = id;
        this.dateV = dateV;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Visite)) {
            return false;
        }
        Visite other = (Visite) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Visites[ id=" + id + " ]";
    }
}
