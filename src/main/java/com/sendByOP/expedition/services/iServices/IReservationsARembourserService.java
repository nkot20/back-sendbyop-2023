package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.entities.RefundableBooking;

import java.util.List;

public interface IReservationsARembourserService {
    public RefundableBooking save(RefundableBooking reservationsARembourser);
    public List<RefundableBooking> findAll();
}
