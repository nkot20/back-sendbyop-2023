package com.sendByOP.expedition.services.IServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.Colis;
import com.sendByOP.expedition.model.Reservation;

import java.util.List;

public interface IColisService {
    public Colis saveColis(Colis colis) throws SendByOpException;
    public void deleteColis(Colis colis) ;
    public Colis findColis(int id) throws SendByOpException;
    public List<Colis> findAllColisByForReservation(Reservation idRe);
}
