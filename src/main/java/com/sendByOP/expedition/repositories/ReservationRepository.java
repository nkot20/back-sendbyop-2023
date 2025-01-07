package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Reservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends CrudRepository<Reservation, Integer> {

    public List<Reservation> findByReserveurOrderByDatereDesc(Client iclient);

    public List<Reservation> findByStatutReExpe(int value);

    public List<Reservation> findByDatere(Date dateRe);

    public Optional<Reservation> findByIdRe(int idRe);

    public List<Reservation> findAllByOrderByDatereDesc();



}
