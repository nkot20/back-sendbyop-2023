package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.entities.Flight;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FlightMapper {
    FlightDto toDto(Flight flight);
    Flight toEntity(FlightDto flightDTO);
}
