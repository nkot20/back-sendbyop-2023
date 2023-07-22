package com.sendByOP.expedition.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "client")
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idp")
    private Integer idp;

    @Column(name = "numcni")
    private Integer numcni;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "datenais")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datenais;

    @Column(name = "pieceid")
    private String pieceid;

    @Column(name = "tel")
    private String tel;

    @Column(name = "email")
    private String email;

    @Column(name = "photo_profil")
    private String photoProfil;


    @Column(name = "pw")
    private String pw;

    @Column(name = "etat_inscription")
    private int etatInscription;

    @Column(name = "valid_email")
    private int validEmail;

    @Column(name = "valid_number")
    private int validNumber;

    @Column(name = "lien_whats_app")
    private String lien;

    @Column(name = "date_insc")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateInsc;

    @Column(name = "pays")
    private String pays;

    @Column(name = "cni_is_upload", nullable = false)
    private int cniisupload;


    @Column(name = "piece_id_is_valid")
    private int pieceidisvalid;

    @Column(name = "iban")
    private String iban;





    public Client() {
    }

    public Client(Integer idp) {
        this.idp = idp;
    }

    public Client(Integer idp, String pw, String keyPw) {
        this.idp = idp;
        this.pw = pw;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idp != null ? idp.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Client)) {
            return false;
        }
        Client other = (Client) object;
        if ((this.idp == null && other.idp != null) || (this.idp != null && !this.idp.equals(other.idp))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Client[ idp=" + idp + " ]";
    }

}
