package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.CountryMapper;
import com.sendByOP.expedition.models.dto.CountryDto;
import com.sendByOP.expedition.models.entities.Country;
import com.sendByOP.expedition.repositories.CountryRepository;
import com.sendByOP.expedition.services.iServices.ICountryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CountryService implements ICountryService {
    private final CountryRepository paysRepository;
    private final CountryMapper countryMapper;

    public CountryDto saveCountry(CountryDto countryDto) {
        Country country = countryMapper.toEntity(countryDto);
        Country savedCountry = paysRepository.save(country);
        return countryMapper.toDto(savedCountry);
    }

    public List<CountryDto> getCountry() {
        List<Country> countries = paysRepository.findAll();
        return countryMapper.toDtoList(countries);
    }

}
