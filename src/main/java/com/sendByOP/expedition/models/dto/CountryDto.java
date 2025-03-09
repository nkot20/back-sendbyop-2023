package com.sendByOP.expedition.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CountryDto {
    private Integer countryId;
    private String name;
    private String createdBy;
    private String updatedBy;
}
