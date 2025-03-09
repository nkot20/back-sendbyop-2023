package com.sendByOP.expedition.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParcelDto {
    private Integer id;
    private String description;
    private Float weightKg;
    private String parcelType;
    private Integer reservationId;  // Seulement l'ID de la réservation
}
