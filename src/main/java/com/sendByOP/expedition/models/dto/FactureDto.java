package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class FactureDto {
    private Integer idfac;
    private Float montantfac;
    private Date datepaie;
    private Integer idRe; // Id de la réservation au lieu de l'objet complet
}
