package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClientDto {
    
    private Integer idp;
    private Integer numcni;
    private String nom;
    private String prenom;
    private Date datenais;
    private String pieceid;
    private String tel;
    private String email;
    private String photoProfil;
    private String pw;
    private int etatInscription;
    private int validEmail;
    private int validNumber;
    private String lien;
    private Date dateInsc;
    private String pays;
    private int cniisupload;
    private int pieceidisvalid;
    private String iban;
    private String address;
}
