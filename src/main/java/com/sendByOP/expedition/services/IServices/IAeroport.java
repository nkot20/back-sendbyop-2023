package com.sendByOP.expedition.services.IServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.Aeroport;

import java.util.List;

public interface IAeroport {
    public Aeroport saveAeroPort(Aeroport aeroport) throws SendByOpException;
    public List<Aeroport> getAllAeroports();
    public Aeroport getAirport(int id) throws SendByOpException;
}
