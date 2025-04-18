package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class RatingDto {
    private Integer id;
    private int score;
    private Integer senderId;
    private Integer customerId;
}
