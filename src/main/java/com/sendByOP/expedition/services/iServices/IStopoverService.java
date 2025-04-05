package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.StopoverDto;

import java.util.List;

public interface IStopoverService {
    StopoverDto addStopover(StopoverDto escaleDTO);
    void deleteStopover(Integer id);
    List<StopoverDto> findByFlightId(int flightId);
}
