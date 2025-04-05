package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.CityDto;
import com.sendByOP.expedition.models.entities.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CityMapper {

    CityMapper INSTANCE = Mappers.getMapper(CityMapper.class);

    @Mapping(source = "country.countryId", target = "countryId")
    CityDto toDto(City city);

    @Mapping(source = "countryId", target = "country.countryId")
    City toEntity(CityDto cityDTO);
}
