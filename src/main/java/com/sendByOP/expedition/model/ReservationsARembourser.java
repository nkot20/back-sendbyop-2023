package com.sendByOP.expedition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Table;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "reservations_a_rembourser")
public class ReservationsARembourser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @JoinColumn(name = "id_reservation", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Reservation reservation;

    @Column(name = "valider")
    private Integer valider;

    public ReservationsARembourser() {
    }

    public ReservationsARembourser(Integer id) {
        this.id = id;
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
        if (!(object instanceof ReservationsARembourser)) {
            return false;
        }
        ReservationsARembourser other = (ReservationsARembourser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.ReservationsARembourser[ id=" + id + " ]";
    }

}
