package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Aeroport;

import java.util.List;

public interface IAeroport {
    public Aeroport saveAeroPort(Aeroport aeroport) throws SendByOpException;
    public List<Aeroport> getAllAeroports();
    public Aeroport getAirport(int id) throws SendByOpException;
}
