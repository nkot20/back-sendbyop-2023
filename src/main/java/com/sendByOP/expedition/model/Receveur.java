package com.sendByOP.expedition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "receveur")
public class Receveur implements Serializable {

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idre")
    private Integer idre;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;


    @Column(name = "tel")
    private String tel;

    @Column(name = "email")
    private String email;



   /* @OneToMany(mappedBy = "idrec")
    private List<Reservation> reservationList;
*/

    /*public List<Reservation> getReservationList() {
        return reservationList;
    }

    public void setReservationList(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }
*/

    public Receveur() {
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
        if (!(object instanceof Receveur)) {
            return false;
        }
        Receveur other = (Receveur) object;
        if ((this.idre == null && other.idre != null) || (this.idre != null && !this.idre.equals(other.idre))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Receveur[ idre=" + idre + " ]";
    }


}
