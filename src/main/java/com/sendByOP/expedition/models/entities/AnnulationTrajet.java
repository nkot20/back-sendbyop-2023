package com.sendByOP.expedition.models.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "annulation_trajet")
public class AnnulationTrajet implements Serializable {

    private static final long serialVersionUID = 1L;
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

    @JoinColumn(name = "id_trajet", referencedColumnName = "idvol")
    @ManyToOne(optional = false)
    private Vol idtrajet;

    @Column(name = "consulter")
    private Integer consulter;

    public AnnulationTrajet() {
    }

    public AnnulationTrajet(Integer idAnnulation) {
        this.idAnnulation = idAnnulation;
    }

    public AnnulationTrajet(Integer idAnnulation, String motif, Date date) {
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
        if (!(object instanceof AnnulationTrajet)) {
            return false;
        }
        AnnulationTrajet other = (AnnulationTrajet) object;
        if ((this.idAnnulation == null && other.idAnnulation != null) || (this.idAnnulation != null && !this.idAnnulation.equals(other.idAnnulation))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.AnnulationTrajet[ idAnnulation=" + idAnnulation + " ]";
    }

}
