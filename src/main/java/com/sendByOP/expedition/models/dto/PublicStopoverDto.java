package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "DTO for public stopover information with detailed location data")
public class PublicStopoverDto {
    
    @Schema(description = "Unique identifier for the stopover", example = "1")
    private Integer id;

    @Schema(description = "Date of the stopover", example = "2023-12-25")
    private Date date;

    @Schema(description = "Time of the stopover", example = "14:30")
    private String hour;

    // Airport Information
    @Schema(description = "Stopover airport name", example = "Dubai International Airport")
    private String airportName;

    @Schema(description = "Stopover airport code", example = "DXB")
    private String airportCode;

    @Schema(description = "Stopover city name", example = "Dubai")
    private String cityName;

    @Schema(description = "Stopover country name", example = "United Arab Emirates")
    private String countryName;
}
