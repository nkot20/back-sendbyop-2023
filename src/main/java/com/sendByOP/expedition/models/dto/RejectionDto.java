package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class RejectionDto {
    private Integer id;
    private String reason;
    private Integer reservationId;
}
