package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.AnnulationReservation;
import com.sendByOP.expedition.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAnnulationReservationRepository extends JpaRepository<AnnulationReservation, Integer> {

    public Optional<AnnulationReservation> findByIdreservation(Reservation idreservatrion);


}
