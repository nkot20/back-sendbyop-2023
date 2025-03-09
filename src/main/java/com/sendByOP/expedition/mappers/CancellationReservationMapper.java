package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.CancellationReservationDto;
import com.sendByOP.expedition.models.entities.CancellationReservation;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CancellationReservationMapper {
    CancellationReservationMapper INSTANCE = Mappers.getMapper(CancellationReservationMapper.class);

    @Mapping(source = "reservation.reservationId", target = "reservationId")
    CancellationReservationDto toDto(CancellationReservation cancellationReservation);

    @Mapping(source = "reservationId", target = "reservation.reservationId")
    CancellationReservation toEntity(CancellationReservationDto cancellationReservationDTO);
}
