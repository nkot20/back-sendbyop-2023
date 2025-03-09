package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.RejectionDto;
import com.sendByOP.expedition.models.entities.Rejection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RejectionMapper {

    RejectionMapper INSTANCE = Mappers.getMapper(RejectionMapper.class);

    @Mapping(source = "reservation.id", target = "reservationId")
    RejectionDto toDto(Rejection rejection);

    @Mapping(source = "reservationId", target = "reservation.id")
    Rejection toEntity(RejectionDto rejectionDTO);
}
