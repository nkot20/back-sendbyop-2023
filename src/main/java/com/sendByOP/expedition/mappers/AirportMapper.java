package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.AirportDto;
import com.sendByOP.expedition.models.entities.Airport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AirportMapper {
    @Mapping(source = "city.cityId", target = "cityId")
    AirportDto toDto(Airport airport);

    @Mapping(source = "cityId", target = "city.cityId")
    Airport toEntity(AirportDto airportDTO);
}
