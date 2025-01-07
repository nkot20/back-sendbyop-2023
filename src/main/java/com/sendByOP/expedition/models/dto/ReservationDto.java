package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ReservationDto implements Serializable {
    private Integer idRe;
    private Date datere = new Date();
    private String heureRe;
    private Integer statutPayement;
    private Integer statutReExpe;
    private String avisClient;
    private String avisExpediteur;
    private int etatReceptionExp;
    private int etatReceptionClient;
    private ReceveurDto receveur;
    private VolDto vol;
    private ClientDto reserveur;
    private int annuler;
    private int stastutPaimentTransporteur;
    private List<ColisDto> colisList;

}
