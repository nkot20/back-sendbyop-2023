package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RefundDto {
    private Integer id;
    private Integer reservationId;  // Id de la réservation associée
}
