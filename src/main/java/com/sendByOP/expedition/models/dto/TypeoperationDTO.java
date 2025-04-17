package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO for operation type information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeoperationDTO {

    @Schema(description = "Unique identifier for the operation type", example = "1")
    private Integer idTypeOperation;

    @Schema(description = "Title or name of the operation type", example = "Delivery")
    private String intitule;
}
