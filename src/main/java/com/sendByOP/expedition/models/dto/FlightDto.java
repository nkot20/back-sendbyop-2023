package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FlightDto {
    private Integer flightId;
    private Date arrivalDate;
    private Date departureDate;
    private String arrivalTime;
    private String departureTime;
    private Integer amountPerKg;
    private Integer kgCount;
    private Integer departureAirportId;
    private Integer arrivalAirportId;
    private Integer clientId;
    private int validationStatus;
    private String preference;
    private Date publicationDate;
    private String image;
    private String depositLocation;
    private String receptionLocation;
    private int cancelled;
}

