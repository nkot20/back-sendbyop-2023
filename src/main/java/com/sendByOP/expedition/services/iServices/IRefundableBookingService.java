package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.dto.RefundableBookingDto;

import java.util.List;

public interface IRefundableBookingService {
    public RefundableBookingDto save(RefundableBookingDto bookingDto) throws SendByOpException;
    public List<RefundableBookingDto> findAll() throws SendByOpException;

    public BookingDto findRefundableBooking(int id) throws SendByOpException;
    public void delete(int id) throws SendByOpException;
}
