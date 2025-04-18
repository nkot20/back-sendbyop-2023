package com.sendByOP.expedition.models.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CancellationTripDto {
    private Integer cancellationId;
    private String reason;
    private Date cancellationDate;
    private Integer tripId;
    private Boolean viewed;
    private String createdBy;
    private String updatedBy;
}
