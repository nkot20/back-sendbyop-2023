package com.sendByOP.expedition.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "reservation")
public class Reservation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_re")
    private Integer idRe;

    @Column(name = "date_re")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datere = new Date();

    @Column(name = "heure_re")
    private String heureRe;


    @Column(name = "statut_payement")
    private Integer statutPayement;

    @Column(name = "statut_re_expe")
    private Integer statutReExpe;

    @Column(name = "avisClient")
    private String avisClient;


    @Column(name = "avis_expediteur")
    private String avisExpediteur;

    @Column(name = "etat_reception_exp")
    private int etatReceptionExp;


    @Column(name = "etat_reception_client")
    private int etatReceptionClient;

    @JoinColumn(name = "idrec", referencedColumnName = "idre")
    @ManyToOne
    private Receveur receveur;

    @JoinColumn(name = "idvol", referencedColumnName = "idvol")
    @ManyToOne(optional = false)
    private Vol vol;

    @JoinColumn(name = "id_client", referencedColumnName = "idp")
    @ManyToOne(optional = false)
    private Client reserveur;

    @Column(name = "annuler")
    private int annuler;

    @Column(name = "statut_payement_transporteur")
    private int stastutPaimentTransporteur;

    @ToString.Exclude

    @OneToMany(mappedBy = "idre", fetch = FetchType.EAGER)
    private List<Colis> colisList;


    public Reservation() {
    }

    public Reservation(Integer idRe) {
        this.idRe = idRe;
    }



    public Reservation(Date dateRe, String heureRe, Integer statutReClient, Integer statutReExpe, Receveur idrec, Vol idvol, Client idClient) {
        this.datere = dateRe;
        this.heureRe = heureRe;

        this.statutPayement = statutReClient;
        this.statutReExpe = statutReExpe;
        this.receveur = idrec;
        this.vol = idvol;
        this.reserveur = idClient;
    }



    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idRe != null ? idRe.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Reservation)) {
            return false;
        }
        Reservation other = (Reservation) object;
        if ((this.idRe == null && other.idRe != null) || (this.idRe != null && !this.idRe.equals(other.idRe))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Reservation[ idRe=" + idRe + " ]";
    }
}
