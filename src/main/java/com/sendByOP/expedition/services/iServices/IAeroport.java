package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Airport;

import java.util.List;

public interface IAeroport {
    public Airport saveAeroPort(Airport aeroport) throws SendByOpException;
    public List<Airport> getAllAeroports();
    public Airport getAirport(int id) throws SendByOpException;
}
