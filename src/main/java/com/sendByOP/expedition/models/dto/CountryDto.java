package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO for country information")  
public class CountryDto {
    @Schema(description = "Unique identifier for the country", example = "1")
    private Integer countryId;

    @Schema(description = "Name of the country", example = "France")
    private String name;

    @Schema(description = "Username who created the country record", example = "admin")
    private String createdBy;

    @Schema(description = "Username who last updated the country record", example = "admin")
    private String updatedBy;
}
