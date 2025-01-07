package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReservationsARembourserDto {
    private Integer id;
    private Integer idReservation;  // Id de la réservation associée
    private Integer valider;        // Indicateur de validation
}
