package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.ParcelDto;
import com.sendByOP.expedition.models.dto.BookingDto;

import java.util.List;

public interface IParcelService {
    public ParcelDto saveParcel(ParcelDto colis) throws SendByOpException;
    public void deleteParcel(ParcelDto colis) ;
    public ParcelDto findParcelById(int id) throws SendByOpException;
    public List<ParcelDto> findAllParcelsByBooking(BookingDto idRe);
}
