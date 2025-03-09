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
    @Mapping(source = "flight.id", target = "flightId")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "parcels", target = "parcelIds", qualifiedByName = "mapParcelsToIds")
    BookingDto toDto(Booking booking);

    @Mapping(source = "receiverId", target = "receiver.id")
    @Mapping(source = "flightId", target = "flight.id")
    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(source = "parcelIds", target = "parcels", ignore = true) // Gestion spéciale côté service
    Booking toEntity(Booking bookingDTO);

    @Named("mapParcelsToIds")
    default List<Integer> mapParcelsToIds(List<Parcel> parcels) {
        return parcels != null ? parcels.stream().map(Parcel::getId).collect(Collectors.toList()) : null;
    }
}
