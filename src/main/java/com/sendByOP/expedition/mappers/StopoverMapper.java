package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.StopoverDto;
import com.sendByOP.expedition.models.entities.Stopover;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StopoverMapper {


     StopoverMapper INSTANCE = Mappers.getMapper(StopoverMapper.class);

     @Mapping(source = "airport.airportId", target = "airportId")
     @Mapping(source = "flight.flightId", target = "flightId")
     StopoverDto toDto(Stopover stopover);

     @Mapping(source = "airportId", target = "airport.airportId")
     @Mapping(source = "flightId", target = "flight.flightId")
     Stopover toEntity(StopoverDto stopoverDTO);
}
