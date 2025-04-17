package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirPortRepository extends JpaRepository<Airport, Integer> {

}
