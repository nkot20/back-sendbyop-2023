package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.AeroPortDto;
import com.sendByOP.expedition.models.entities.Aeroport;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AeroportMapper {
    Aeroport toEntity(AeroPortDto aeroPortDTO);

    AeroPortDto toDto(Aeroport aeroport);

    void copy(AeroPortDto aeroPortDTO, @MappingTarget Aeroport aeroport);
}
