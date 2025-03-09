package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.RatingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface Rating {

    Rating INSTANCE = Mappers.getMapper(Rating.class);

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "customer.id", target = "customerId")
    RatingDto toDto(Rating rating);

    @Mapping(source = "senderId", target = "sender.id")
    @Mapping(source = "customerId", target = "customer.id")
    Rating toEntity(RatingDto ratingDTO);
}