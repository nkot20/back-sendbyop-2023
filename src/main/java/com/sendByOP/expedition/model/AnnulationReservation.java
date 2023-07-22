package com.sendByOP.expedition.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@Table(name = "annulation_reservation")
public class AnnulationReservation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_annulation")
    private Integer idAnnulation;

    @Basic(optional = false)
    @Column(name = "motif")
    private String motif;

    @Basic(optional = false)
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @JoinColumn(name = "id_reservation", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Reservation idreservation;

    @Column(name = "consulter")
    private Integer consulter;

    public AnnulationReservation() {
    }

    public AnnulationReservation(Integer idAnnulation) {
        this.idAnnulation = idAnnulation;
    }

    public AnnulationReservation(Integer idAnnulation, String motif, Date date) {
        this.idAnnulation = idAnnulation;
        this.motif = motif;
        this.date = date;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idAnnulation != null ? idAnnulation.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AnnulationReservation)) {
            return false;
        }
        AnnulationReservation other = (AnnulationReservation) object;
        if ((this.idAnnulation == null && other.idAnnulation != null) || (this.idAnnulation != null && !this.idAnnulation.equals(other.idAnnulation))) {
            return false;
        }
        return true;
    }


}
