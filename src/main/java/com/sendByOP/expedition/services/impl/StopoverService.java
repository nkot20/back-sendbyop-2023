package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.mappers.FlightMapper;
import com.sendByOP.expedition.mappers.StopoverMapper;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.StopoverDto;
import com.sendByOP.expedition.models.entities.Stopover;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.repositories.FlightRepository;
import com.sendByOP.expedition.repositories.StopoverRepository;
import com.sendByOP.expedition.services.iServices.IStopoverService;
import com.sendByOP.expedition.services.iServices.IVolService;
import com.sendByOP.expedition.web.exceptions.escale.ImpossibleEffectuerEscaleException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StopoverService implements IStopoverService {
    private final StopoverRepository stopoverRepository;
    private final StopoverMapper stopoverMapper;
    private final FlightMapper flightMapper;
    private final FlightRepository flightRepository;


    @Override
    public void deleteStopover(Integer id) {
        Optional<Stopover> stopover = stopoverRepository.findById(id);
        stopover.ifPresent(stopoverRepository::delete);
    }

    @Override
    public List<StopoverDto> findByFlightId(int id) {
        FlightDto flightDto = getVolById(id);
        if (flightDto == null) {
            throw new ImpossibleEffectuerEscaleException("Flight not found for ID: " + id);
        }
        Flight flight = flightMapper.toEntity(flightDto);
        List<Stopover> stopovers = stopoverRepository.findByFlight(flight);
        return stopovers.stream()
                .map(stopoverMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public StopoverDto addStopover(StopoverDto stopoverDTO) {
        if (stopoverDTO == null) {
            throw new ImpossibleEffectuerEscaleException(ErrorInfo.INTERNAL_ERROR.getMessage());
        }
        Stopover stopover = stopoverMapper.toEntity(stopoverDTO);
        Stopover savedStopover = stopoverRepository.save(stopover);
        return stopoverMapper.toDto(savedStopover);
    }

    private FlightDto getVolById(int id) {
        log.debug("Fetching flight with id: {}", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with id: {}", id);
                    return new EntityNotFoundException("Flight not found with id: " + id);
                });
        return flightMapper.toDto(flight);
    }

}
