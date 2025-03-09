package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.ReviewDto;
import com.sendByOP.expedition.models.entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {CustomerMapper.class}) // Utilisation de ClientMapper pour mapper les objets Client
public interface ReviewMapper {

    ReviewMapper INSTANCE = Mappers.getMapper(ReviewMapper.class);

    @Mapping(source = "transporter.id", target = "transporterId")
    @Mapping(source = "shipper.id", target = "shipperId")
    ReviewDto toDto(Review review);

    @Mapping(source = "transporterId", target = "transporter.id")
    @Mapping(source = "shipperId", target = "shipper.id")
    Review toEntity(ReviewDto reviewDto);
}