package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.CountryDto;

import java.util.List;

public interface ICountryService {
    CountryDto saveCountry(CountryDto countryDto);
    List<CountryDto> getCountry();
}
