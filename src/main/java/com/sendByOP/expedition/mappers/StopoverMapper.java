package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.StopoverDto;
import com.sendByOP.expedition.models.entities.Stopover;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

// Utilisation des autres mappers
public interface StopoverMapper {


     StopoverMapper INSTANCE = Mappers.getMapper(StopoverMapper.class);

     @Mapping(source = "airport.id", target = "airportId")
     @Mapping(source = "flight.id", target = "flightId")
     StopoverDto toDto(Stopover stopover);

     @Mapping(source = "airportId", target = "airport.id")
     @Mapping(source = "flightId", target = "flight.id")
     Stopover toEntity(StopoverDto stopoverDTO);
}
