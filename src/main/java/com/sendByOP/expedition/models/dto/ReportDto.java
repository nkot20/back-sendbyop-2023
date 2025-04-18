package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class ReportDto {
    private Integer reportId;
    private int flightId;
    private String reason;
    private Date date;
}
