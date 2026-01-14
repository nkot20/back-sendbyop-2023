package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.mappers.CityMapper;
import com.sendByOP.expedition.models.dto.CityDto;
import com.sendByOP.expedition.models.entities.City;
import com.sendByOP.expedition.models.entities.Country;
import com.sendByOP.expedition.repositories.CityRepository;
import com.sendByOP.expedition.repositories.CountryRepository;
import com.sendByOP.expedition.services.iServices.ICityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements ICityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository; // Nécessaire pour récupérer un pays
    private final CityMapper cityMapper;


    @Override
    public List<CityDto> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(cityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CityDto getCityById(Integer id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id " + id));
        return cityMapper.toDto(city);
    }

    @Override
    public CityDto createCity(CityDto cityDto) {
        // Récupération du pays
        Country country = countryRepository.findById(cityDto.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found with id " + cityDto.getCountryId()));

        // Conversion DTO -> Entity
        City city = cityMapper.toEntity(cityDto);
        city.setCountry(country);

        City savedCity = cityRepository.save(city);
        return cityMapper.toDto(savedCity);
    }

    @Override
    public CityDto updateCity(Integer id, CityDto cityDto) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id " + id));

        Country country = countryRepository.findById(cityDto.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found with id " + cityDto.getCountryId()));

        existingCity.setName(cityDto.getName());
        existingCity.setCountry(country);
        existingCity.setUpdatedBy(cityDto.getUpdatedBy());

        City updatedCity = cityRepository.save(existingCity);
        return cityMapper.toDto(updatedCity);
    }

    @Override
    public void deleteCity(Integer id) {
        if (!cityRepository.existsById(id)) {
            throw new RuntimeException("City not found with id " + id);
        }
        cityRepository.deleteById(id);
    }
}
