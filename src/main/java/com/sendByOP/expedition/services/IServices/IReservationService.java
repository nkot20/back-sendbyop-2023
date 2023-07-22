package com.sendByOP.expedition.services.IServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.Client;
import com.sendByOP.expedition.model.Refus;
import com.sendByOP.expedition.model.Reservation;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.List;

public interface IReservationService {

    public Reservation saveReservation(Reservation reservation) throws SendByOpException;
    public Reservation saveReservationWithColis(Reservation reservation) throws SendByOpException;
    public void deleteReservation(Reservation reservation);
    public Reservation updateReservation(Reservation reservation) throws SendByOpException;
    public Reservation getReservation(int id) throws SendByOpException;
    public List<Reservation> reservationList();
    public List<Reservation> clientDestinatorReservationList(Client idClient);
    public List<Reservation> getReservationByDate(Date date);
    public Reservation refuserReservation(Reservation reservation, Refus refus) throws MessagingException, UnsupportedEncodingException, SendByOpException;


}
