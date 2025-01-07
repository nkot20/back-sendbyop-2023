package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Aeroport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAeroportRepository extends JpaRepository<Aeroport, Integer> {

    public Optional<Aeroport> findByIdaero(int id);
}
