package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.RejectionDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.List;

public interface IReservationService {

    public BookingDto saveReservation(BookingDto reservation) throws SendByOpException;
    public BookingDto saveReservationWithColis(BookingDto reservation) throws SendByOpException;
    public void deleteReservation(int id);
    public BookingDto updateReservation(BookingDto reservation) throws SendByOpException;
    public BookingDto getReservation(int id) throws SendByOpException;
    public List<BookingDto> reservationList();
    public List<BookingDto> clientDestinatorReservationList(int idClient);
    public List<BookingDto> getReservationByDate(Date date);
    public BookingDto refuserReservation(BookingDto reservation, RejectionDto refus)
            throws MessagingException, UnsupportedEncodingException, SendByOpException;


}
