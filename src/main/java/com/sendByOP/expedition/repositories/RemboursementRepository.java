package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Remboursement;
import com.sendByOP.expedition.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RemboursementRepository extends JpaRepository<Remboursement, Integer> {

    public Optional<Remboursement> findByReservation(Reservation reservation);

}
