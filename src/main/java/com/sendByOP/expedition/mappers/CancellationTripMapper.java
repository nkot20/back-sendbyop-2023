package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.CancellationTripDto;
import com.sendByOP.expedition.models.entities.CancellationTrip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {FlightMapper.class}) // Utilisation de VolMapper pour mapper les objets Vol
public interface CancellationTripMapper {

    CancellationTripMapper INSTANCE = Mappers.getMapper(CancellationTripMapper.class);

    @Mapping(source = "trip.flightId", target = "tripId")
    CancellationTripDto toDto(CancellationTrip cancellationTrip);

    @Mapping(source = "tripId", target = "trip.flightId")
    CancellationTrip toEntity(CancellationTripDto cancellationTripDto);
}