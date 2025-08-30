package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Booking;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends CrudRepository<Booking, Integer> {

    List<Booking> findByCustomerIdOrderByBookingDateDesc(Integer customerId);

    List<Booking> findByExpeditionStatus(int value);

    List<Booking> findByBookingDate(Date bookingDate);

    Optional<Booking> findById(Integer id);

    List<Booking> findAllByOrderByBookingDateDesc();

    List<Booking> findByFlightFlightIdAndPaymentStatus(Integer flightId, Integer paymentStatus);

}
