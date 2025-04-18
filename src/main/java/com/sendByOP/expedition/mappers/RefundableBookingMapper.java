package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.RefundableBookingDto;
import com.sendByOP.expedition.models.entities.RefundableBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefundableBookingMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    RefundableBookingDto toDto(RefundableBooking refundableBooking);

    @Mapping(source = "bookingId", target = "booking.id")
    RefundableBooking toEntity(RefundableBookingDto refundableBookingDTO);
}
