package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.ReportDto;
import com.sendByOP.expedition.models.entities.Report;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    ReportDto toDto(Report report);
    Report toEntity(ReportDto reportDTO);
}
