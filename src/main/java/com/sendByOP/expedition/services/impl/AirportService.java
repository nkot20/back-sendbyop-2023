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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
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
        List<Airport> airportEntities = airportRepository.findAll();
        return airportEntities.stream()
                .map(airportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AirportDto getAirport(int id) throws SendByOpException {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
        return airportMapper.toDto(airport);
    }
}
