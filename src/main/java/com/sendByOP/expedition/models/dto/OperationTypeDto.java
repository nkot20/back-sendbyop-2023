package com.sendByOP.expedition.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationTypeDto {
    private Integer id;
    private String title;
}