package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancellationReservationDto implements Serializable {

    private Integer cancellationId;
    private String reason;
    private Date cancellationDate;
    private Integer reservationId;
    private Boolean viewed;
    private String createdBy;
    private String updatedBy;


}
