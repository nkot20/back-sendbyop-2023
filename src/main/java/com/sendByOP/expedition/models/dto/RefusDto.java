package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class RefusDto {
    private Integer idre;
    private String motif;
    private Integer idRe;  // Id de la réservation associée
}
