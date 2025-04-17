package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for city information")
public class CityDto {
    @Schema(description = "Unique identifier for the city", example = "1")
    private Integer cityId;

    @NotNull(message = "City name cannot be null")
    @Schema(description = "Name of the city", example = "Paris")
    private String name;

    @NotNull(message = "Country ID cannot be null")
    @Schema(description = "ID of the country where the city is located", example = "1")
    private Integer countryId;

    @Schema(description = "Username who created the city record", example = "admin")
    private String createdBy;

    @Schema(description = "Username who last updated the city record", example = "admin")
    private String updatedBy;
}