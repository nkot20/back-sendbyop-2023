package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.entities.Reservation;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnulationReservationDto implements Serializable {

    private Integer idAnnulation;
    private String motif;
    private Date date;
    private Reservation idreservation;

    private Integer consulter;

    public AnnulationReservationDto(Integer idAnnulation) {
        this.idAnnulation = idAnnulation;
    }


}
