package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
@Builder
public class OperationDto {
    private Integer id;
    private Date operationDate;
    private Integer operationTypeId; // Only the ID of the operation type
    private Integer reservationId;
}
