package com.sendByOP.expedition.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AirportDto {
    private Integer airportId;
    private String name;
    private Integer cityId;
    private String createdBy;
    private String updatedBy;
    private String iataCode;
}
