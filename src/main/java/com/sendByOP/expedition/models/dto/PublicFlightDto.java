package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "DTO for public flight information with detailed location data")
public class PublicFlightDto {
    
    @Schema(description = "Unique identifier for the flight", example = "1")
    private Integer flightId;

    @Schema(description = "Date of flight arrival", example = "2023-12-25")
    private Date arrivalDate;

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

    @Schema(description = "Available weight capacity in kilograms", example = "75")
    private Integer availableKg;

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

    // Departure Airport Information
    @Schema(description = "Departure airport name", example = "Charles de Gaulle Airport")
    private String departureAirportName;

    @Schema(description = "Departure airport code", example = "CDG")
    private String departureAirportCode;

    @Schema(description = "Departure city name", example = "Paris")
    private String departureCityName;

    @Schema(description = "Departure country name", example = "France")
    private String departureCountryName;

    // Arrival Airport Information
    @Schema(description = "Arrival airport name", example = "John F. Kennedy International Airport")
    private String arrivalAirportName;

    @Schema(description = "Arrival airport code", example = "JFK")
    private String arrivalAirportCode;

    @Schema(description = "Arrival city name", example = "New York")
    private String arrivalCityName;

    @Schema(description = "Arrival country name", example = "United States")
    private String arrivalCountryName;

    // Customer Information (limited for public API)
    @Schema(description = "Customer first name", example = "John")
    private String customerFirstName;

    @Schema(description = "Customer last name", example = "Doe")
    private String customerLastName;

    // Stopovers Information
    @Schema(description = "List of stopovers for this flight")
    private List<PublicStopoverDto> stopovers;
}
