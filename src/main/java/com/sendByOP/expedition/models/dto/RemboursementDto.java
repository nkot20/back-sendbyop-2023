package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RemboursementDto {
    private Integer idRem;
    private Integer idReservation;  // Id de la réservation associée
}
