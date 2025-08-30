package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.AirportMapper;
import com.sendByOP.expedition.models.dto.AirportDto;
import com.sendByOP.expedition.models.entities.Airport;
import com.sendByOP.expedition.repositories.AirPortRepository;
import com.sendByOP.expedition.services.iServices.IAirPortService;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AirportService implements IAirPortService {

    private final AirPortRepository airportRepository;
    private final AirportMapper airportMapper;

    @Override
    public AirportDto saveAeroPort(AirportDto airportDto) throws SendByOpException {
        CHeckNull.checkIntitule(airportDto.getName());
        Airport airportEntity = airportMapper.toEntity(airportDto);
        Airport savedAirport = airportRepository.save(airportEntity);
        return airportMapper.toDto(savedAirport);
    }

    @Override
    public List<AirportDto> getAllAirport() {
        log.info("Fetching all airports");
        List<Airport> airportEntities = airportRepository.findAll();
        log.info("Airport number {}", airportEntities.size());

        return airportEntities.stream()
                .map(airport -> {
                    // Conversion de base
                    AirportDto dto = airportMapper.toDto(airport);
                    // Remplissage du nom de la ville
                    if (airport.getCity() != null) {
                        dto.setCity(airport.getCity().getName());
                        // Remplissage du nom du pays
                        if (airport.getCity().getCountry() != null) {
                            dto.setCountry(airport.getCity().getCountry().getName());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public AirportDto getAirport(int id) throws SendByOpException {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
        AirportDto dto = airportMapper.toDto(airport);
        if (airport.getCity() != null) {
            dto.setCity(airport.getCity().getName());
            if (airport.getCity().getCountry() != null) {
                dto.setCountry(airport.getCity().getCountry().getName());
            }
        }
        return dto;
    }
}
