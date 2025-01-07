package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class SignalementDto {
    private Integer idS;       // Identifiant du signalement
    private int idVol;         // Identifiant du vol associé (ou remplacer par VolDto si relation)
    private String motif;      // Motif du signalement
    private Date date;         // Date du signalement
}
