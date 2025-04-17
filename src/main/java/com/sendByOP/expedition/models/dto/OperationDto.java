package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO for operation information")
public class OperationDto {
    @Schema(description = "Unique identifier for the operation", example = "1")
    private Integer id;

    @NotNull(message = "Operation date is required")
    @Schema(description = "Date when the operation was performed", example = "2023-12-25")
    private Date operationDate;

    @NotNull(message = "Operation type ID is required")
    @Schema(description = "Identifier of the operation type", example = "1")
    private Integer operationTypeId;

    @NotNull(message = "Reservation ID is required")
    @Schema(description = "Identifier of the associated reservation", example = "1")
    private Integer reservationId;
}
