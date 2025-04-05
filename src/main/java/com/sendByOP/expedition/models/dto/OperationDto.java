package com.sendByOP.expedition.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OperationDto {
    private Integer id;

    @NotNull(message = "Operation date is required")
    private Date operationDate;

    @NotNull(message = "Operation type ID is required")
    private Integer operationTypeId;

    @NotNull(message = "Reservation ID is required")
    private Integer reservationId;
}
