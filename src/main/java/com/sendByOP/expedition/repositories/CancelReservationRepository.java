package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.CancellationReservation;
import com.sendByOP.expedition.models.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CancelReservationRepository extends JpaRepository<CancellationReservation, Integer> {

    public Optional<CancellationReservation> findByReservation(Booking booking);


}
