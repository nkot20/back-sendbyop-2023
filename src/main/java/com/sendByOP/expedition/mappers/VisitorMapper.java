package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.VisitorDto;
import com.sendByOP.expedition.models.entities.Visite;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VisitorMapper {
    VisitorMapper INSTANCE = Mappers.getMapper(VisitorMapper.class);

    VisitorDto toDto(Visite visite);

    Visite toEntity(VisitorDto visitorDto);
}