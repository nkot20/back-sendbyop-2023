package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.CityDto;
import com.sendByOP.expedition.models.entities.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CityMapper {
    @Mapping(source = "country.countryId", target = "countryId")
    CityDto toDto(City city);

    @Mapping(source = "countryId", target = "country.countryId")
    City toEntity(CityDto cityDTO);
}
