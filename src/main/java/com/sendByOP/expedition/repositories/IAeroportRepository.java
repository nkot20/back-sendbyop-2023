package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAeroportRepository extends JpaRepository<Airport, Integer> {

    public Optional<Airport> findByIdaero(int id);
}
