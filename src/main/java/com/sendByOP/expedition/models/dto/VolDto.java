package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VolDto {
    private Integer idvol;
    private Date datearrive;
    private Date datedepart;
    private String heurearriv;
    private String heuredepart;
    private Integer montantkilo;
    private Integer nbkilo;
    private AeroPortDto idaeroDepart;
    private AeroPortDto idAeroArrive;
    private ClientDto idclient;
    private int etatvalidation;
    private String preference;
    private Date datepublication;
    private String image;
    private String lieuDepot;
    private String lieuReception;
    private int annuler;

}

