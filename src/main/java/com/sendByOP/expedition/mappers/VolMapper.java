package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.VolDto;
import com.sendByOP.expedition.models.entities.Vol;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface VolMapper {

    Vol toEntity(VolDto volDTO);

    VolDto toDto(Vol vol);

    void copy(VolDto volDTO, @MappingTarget Vol vol);
}
