package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.Booking;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends CrudRepository<Booking, Integer> {

    public List<Booking> findByReserveurOrderByDatereDesc(Customer iclient);

    public List<Booking> findByStatutReExpe(int value);

    public List<Booking> findByDatere(Date dateRe);

    public Optional<Booking> findByIdRe(int idRe);

    public List<Booking> findAllByOrderByDatereDesc();



}
