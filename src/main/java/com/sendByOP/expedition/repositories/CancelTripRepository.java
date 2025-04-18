package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.CancellationTrip;
import com.sendByOP.expedition.models.entities.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CancelTripRepository extends JpaRepository<CancellationTrip, Integer> {

    public Optional<CancellationTrip> findByTrip(Flight vol);

}
