package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.ParcelDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ParcelMapper {
    com.sendByOP.expedition.models.entities.Parcel toEntity(ParcelDto colisDto);

    ParcelDto toDto(com.sendByOP.expedition.models.entities.Parcel colis);

    void copy(ParcelDto colisDto, @MappingTarget com.sendByOP.expedition.models.entities.Parcel colis);

    List<ParcelDto> toDtoList(List<com.sendByOP.expedition.models.entities.Parcel> colis);
}
