package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PaymentDto;
import com.sendByOP.expedition.models.dto.BookingDto;

import java.util.List;

public interface IPaymentService {
    public BookingDto calculMontantFacture(int idRe) throws SendByOpException;
    public PaymentDto save(PaymentDto paiement);
    public List<PaymentDto> getAll();
    public List<PaymentDto> getPaymentsByClient(String email) throws SendByOpException;
    public BookingDto processPayment(int reservationId, PaymentDto paiement) throws SendByOpException;
}
