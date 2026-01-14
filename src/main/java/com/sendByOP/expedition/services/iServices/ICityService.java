package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.CityDto;

import java.util.List;

public interface ICityService {
    List<CityDto> getAllCities();
    CityDto getCityById(Integer id);
    CityDto createCity(CityDto cityDTO);
    CityDto updateCity(Integer id, CityDto cityDTO);
    void deleteCity(Integer id);
}
