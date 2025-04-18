package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "DTO for flight information")  
public class FlightDto {
    @Schema(description = "Unique identifier for the flight", example = "1")
    private Integer flightId;

    @NotNull(message = "Arrival date cannot be null")
    @Schema(description = "Date of flight arrival", example = "2023-12-25")
    private Date arrivalDate;

    @NotNull(message = "Departure date cannot be null")
    @Schema(description = "Date of flight departure", example = "2023-12-25")
    private Date departureDate;

    @Schema(description = "Time of flight arrival", example = "15:30")
    private String arrivalTime;

    @Schema(description = "Time of flight departure", example = "10:30")
    private String departureTime;

    @Schema(description = "Cost per kilogram for shipping", example = "10")
    private Integer amountPerKg;

    @Schema(description = "Total weight capacity in kilograms", example = "100")
    private Integer kgCount;

    @NotNull(message = "Departure airport ID cannot be null")
    @Schema(description = "ID of the departure airport", example = "1")
    private Integer departureAirportId;

    @NotNull(message = "Arrival airport ID cannot be null")
    @Schema(description = "ID of the arrival airport", example = "2")
    private Integer arrivalAirportId;

    @Schema(description = "ID of the client associated with the flight", example = "1")
    private Integer clientId;

    @Schema(description = "Status of flight validation (0: Pending, 1: Validated, 2: Rejected)", example = "1")
    private int validationStatus;

    @Schema(description = "Special preferences or requirements for the flight", example = "Fragile items only")
    private String preference;

    @Schema(description = "Date when the flight was published", example = "2023-12-20")
    private Date publicationDate;

    @Schema(description = "URL or path to flight-related image", example = "flight123.jpg")
    private String image;

    @Schema(description = "Location where items should be deposited", example = "Terminal 2, Counter 5")
    private String depositLocation;

    @Schema(description = "Location where items can be received", example = "Terminal 1, Baggage Claim 3")
    private String receptionLocation;

    @Schema(description = "Flight cancellation status (0: Active, 1: Cancelled)", example = "0")
    private int cancelled;
}

