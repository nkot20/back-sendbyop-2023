package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.RefundableBookingDto;

import java.util.List;

public interface IRefundableBookingService {
    public RefundableBookingDto save(RefundableBookingDto reservationsARembourser);
    public List<RefundableBookingDto> findAll();
}
