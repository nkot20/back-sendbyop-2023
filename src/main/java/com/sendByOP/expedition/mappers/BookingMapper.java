package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.Parcel;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "flight.flightId", target = "flightId")
    @Mapping(source = "customer.id", target = "customerId")
    BookingDto toDto(Booking booking);

    @Mapping(source = "receiverId", target = "receiver.id")
    @Mapping(source = "flightId", target = "flight.flightId")
    @Mapping(source = "customerId", target = "customer.id")
    Booking toEntity(BookingDto bookingDTO);

}
