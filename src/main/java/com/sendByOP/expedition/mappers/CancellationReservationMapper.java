package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.CancellationReservationDto;
import com.sendByOP.expedition.models.entities.CancellationReservation;
import org.mapstruct.*;

@Mapper
public interface CancellationReservationMapper {

    @Mapping(source = "reservation.id", target = "reservationId")
    CancellationReservationDto toDto(CancellationReservation cancellationReservation);

    @Mapping(source = "reservationId", target = "reservation.id")
    CancellationReservation toEntity(CancellationReservationDto cancellationReservationDTO);
}
