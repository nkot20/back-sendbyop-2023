package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.AirportDto;
import com.sendByOP.expedition.models.entities.Airport;

import java.util.List;

public interface IAeroport {
    public AirportDto saveAeroPort(AirportDto aeroport) throws SendByOpException;
    public List<AirportDto> getAllAeroports();
    public AirportDto getAirport(int id) throws SendByOpException;
}
