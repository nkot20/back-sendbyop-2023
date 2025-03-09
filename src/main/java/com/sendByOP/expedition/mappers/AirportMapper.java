package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.AirportDto;
import com.sendByOP.expedition.models.entities.Airport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AirportMapper {

    AirportMapper INSTANCE = Mappers.getMapper(AirportMapper.class);

    @Mapping(source = "city.id", target = "cityId")
    AirportDto toDto(Airport airport);

    @Mapping(source = "cityId", target = "city.id")
    Airport toEntity(AirportDto airportDTO);
}
