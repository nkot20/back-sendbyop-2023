package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EscaleDto {
    private Integer idEscale;
    private Date datees;
    private String heure;
    private AeroPortDto idaero;
    private VolDto idvol;

}