package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.io.Serializable;


@Data
@Builder
public class ColisDto implements Serializable {

    private String description;
    private Float kilo;
    private String typeColis;
    private ReservationDto idre;


}
