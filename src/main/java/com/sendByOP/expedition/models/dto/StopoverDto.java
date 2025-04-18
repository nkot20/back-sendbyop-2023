package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StopoverDto {
    private Integer id;
    private Date date;
    private String hour;
    private Integer airportId;  // Only the ID of the airport
    private Integer flightId;

}