package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.FlightMapper;
import com.sendByOP.expedition.mappers.StopoverMapper;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.StopoverDto;
import com.sendByOP.expedition.models.entities.Stopover;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.repositories.StopoverRepository;
import com.sendByOP.expedition.services.iServices.IStopoverService;
import com.sendByOP.expedition.services.iServices.IVolService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StopoverService implements IStopoverService {
    private final StopoverRepository stopoverRepository;
    private final StopoverMapper stopoverMapper;
    private final IVolService flightService;
    private final FlightMapper flightMapper;

    @Override
    public StopoverDto addStopover(StopoverDto stopoverDTO) {
        Stopover stopover = stopoverMapper.toEntity(stopoverDTO);
        Stopover savedStopover = stopoverRepository.save(stopover);
        return stopoverMapper.toDto(savedStopover);
    }

    @Override
    public void deleteStopover(Integer id) {
        Optional<Stopover> stopover = stopoverRepository.findById(id);
        stopover.ifPresent(stopoverRepository::delete);
    }

    @Override
    public List<StopoverDto> findByFlightId(int id) {
        FlightDto flightDto = flightService.getVolById(id);
        Flight flight = flightMapper.toEntity(flightDto);
        List<Stopover> stopovers = stopoverRepository.findByFlight(flight);
        return stopovers.stream()
                .map(stopoverMapper::toDto)
                .collect(Collectors.toList());
    }

}
