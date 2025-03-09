package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.ParcelDto;
import com.sendByOP.expedition.models.dto.BookingDto;

import java.util.List;

public interface IColisService {
    public ParcelDto saveColis(ParcelDto colis) throws SendByOpException;
    public void deleteColis(ParcelDto colis) ;
    public ParcelDto findColis(int id) throws SendByOpException;
    public List<ParcelDto> findAllColisByForReservation(BookingDto idRe);
}
