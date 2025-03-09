package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.StopoverDto;
import com.sendByOP.expedition.models.entities.Flight;

import java.util.List;
import java.util.Optional;

public interface IEscaleService {
    StopoverDto addEscale(StopoverDto escaleDTO);
    void deleteEscale(Integer id);
    List<StopoverDto> findByIdvol(Optional<Flight> vol);
}
