package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.RefundDto;
import com.sendByOP.expedition.models.entities.Refund;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RefundMapper {

    RefundMapper INSTANCE = Mappers.getMapper(RefundMapper.class);

    @Mapping(source = "reservation.id", target = "reservationId")
    RefundDto toDto(Refund refund);

    @Mapping(source = "reservationId", target = "reservation.id")
    Refund toEntity(RefundDto refundDTO);
}
