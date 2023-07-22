package com.sendByOP.expedition.model;

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
@Table(name = "paiement")
public class Paiement implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_paiement")
    private Integer idPaiement;

    @Basic(optional = false)
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @JoinColumn(name = "id_client", referencedColumnName = "idp")
    @ManyToOne(optional = false)
    private Client client;

    @JoinColumn(name = "montant")
    private Double montant;

    @JoinColumn(name = "type_paiment", referencedColumnName = "id_type")
    @ManyToOne(optional = false)
    private TypeVirement typePaiment;

    public Paiement() {
    }

    public Paiement(Integer idPaiement) {
        this.idPaiement = idPaiement;
    }

    public Paiement(Integer idPaiement, Date date) {
        this.idPaiement = idPaiement;
        this.date = date;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPaiement != null ? idPaiement.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Paiement)) {
            return false;
        }
        Paiement other = (Paiement) object;
        if ((this.idPaiement == null && other.idPaiement != null) || (this.idPaiement != null && !this.idPaiement.equals(other.idPaiement))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Paiment[ idPaiement=" + idPaiement + " ]";
    }
}
