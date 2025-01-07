package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.AnnulationReservation;
import com.sendByOP.expedition.models.entities.Reservation;

public interface IAnnulationReservationService {
    public AnnulationReservation save(AnnulationReservation annulationReservation) throws SendByOpException;
    public Reservation saveWithReservation(AnnulationReservation annulationReservation) throws SendByOpException;
    public AnnulationReservation findByReservation(Reservation id) throws SendByOpException;
    public void delete(AnnulationReservation annulationReservation);
}
