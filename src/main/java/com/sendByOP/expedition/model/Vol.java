package com.sendByOP.expedition.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "vol")
public class Vol implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Basic(optional = false)
        @Column(name = "idvol")
        private Integer idvol;

        @Column(name = "datearrive")
        @Temporal(TemporalType.TIMESTAMP)
        private Date datearrive;

        @Column(name = "datedepart")
        @Temporal(TemporalType.TIMESTAMP)
        private Date datedepart;

        @Column(name = "heurearriv")
        private String heurearriv;

        @Column(name = "heuredepart")
        private String heuredepart;

        @Column(name = "montantkilo")
        private Integer montantkilo;

        @Column(name = "nbkilo")
        private Integer nbkilo;

        @JoinColumn(name = "idaero_depart", referencedColumnName = "idaero")
        @ManyToOne(optional = false)
        private Aeroport idaeroDepart;

        @JoinColumn(name = "id_aero_arrive", referencedColumnName = "idaero")
        @ManyToOne(optional = false)
        private Aeroport idAeroArrive;

        @JoinColumn(name = "idclient", referencedColumnName = "idp")
        @ManyToOne
        @JsonIgnoreProperties(value = {"applications", "hibernateLazyInitializer"})
        private Client idclient;

        @Basic(optional = false)
        @Column(name = "etat_validation")
        private int etatvalidation;

        @Column(name = "preference")
        private String preference;

        @Column(name = "date_publication")
        @Temporal(TemporalType.TIMESTAMP)
        private Date datepublication;

        @Column(name = "image")
        private String image;

        @Column(name = "lieu_depot")
        private String lieuDepot;

        @Column(name = "lieu_reception")
        private String lieuReception;

        @Column(name = "annuler")
        private int annuler;


        @Override
        public int hashCode() {
                int hash = 0;
                hash += (idvol != null ? idvol.hashCode() : 0);
                return hash;
        }

        @Override
        public boolean equals(Object object) {
                // TODO: Warning - this method won't work in the case the id fields are not set
                if (!(object instanceof Vol)) {
                        return false;
                }
                Vol other = (Vol) object;
                if ((this.idvol == null && other.idvol != null) || (this.idvol != null && !this.idvol.equals(other.idvol))) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                return "com.sendByOP.sendByOp.model.Vol[ idvol=" + idvol + " ]";
        }
}
