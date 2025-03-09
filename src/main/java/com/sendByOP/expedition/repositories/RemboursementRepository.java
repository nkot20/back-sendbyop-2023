package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Refund;
import com.sendByOP.expedition.models.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RemboursementRepository extends JpaRepository<Refund, Integer> {

    public Optional<Refund> findByReservation(Booking reservation);

}
