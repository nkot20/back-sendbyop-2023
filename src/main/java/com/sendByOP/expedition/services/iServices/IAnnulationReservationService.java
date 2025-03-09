package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CancellationReservationDto;
import com.sendByOP.expedition.models.dto.BookingDto;

public interface IAnnulationReservationService {
    public CancellationReservationDto save(CancellationReservationDto annulationReservation) throws SendByOpException;
    public BookingDto saveWithReservation(CancellationReservationDto annulationReservation) throws SendByOpException;
    public CancellationReservationDto findByReservation(BookingDto id) throws SendByOpException;
    public void delete(CancellationReservationDto annulationReservation);
}
