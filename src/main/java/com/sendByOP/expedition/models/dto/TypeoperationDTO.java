package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring Typeoperation data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeoperationDTO {

    // Identifier of the type operation
    private Integer idTypeOperation;

    // Title or name of the type operation
    private String intitule;
}
