package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO for airport information")  
public class AirportDto {
    @Schema(description = "Unique identifier for the airport", example = "1")
    private Integer airportId;

    @NotNull(message = "Airport name cannot be null")
    @Schema(description = "Name of the airport", example = "John F. Kennedy International Airport")
    private String name;

    @NotNull(message = "City ID cannot be null")
    @Schema(description = "ID of the city where airport is located", example = "1")
    private Integer cityId;

    @Schema(description = "Username who created the airport record", example = "admin")
    private String createdBy;

    @Schema(description = "Username who last updated the airport record", example = "admin")
    private String updatedBy;

    @Schema(description = "IATA code of the airport", example = "JFK")
    private String iataCode;
}
