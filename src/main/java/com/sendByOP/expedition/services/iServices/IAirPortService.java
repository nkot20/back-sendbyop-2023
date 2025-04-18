package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.AirportDto;

import java.util.List;

public interface IAirPortService {
    public AirportDto saveAeroPort(AirportDto aeroport) throws SendByOpException;
    public List<AirportDto> getAllAirport();
    public AirportDto getAirport(int id) throws SendByOpException;
}
