package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.AnnulationTrajet;
import com.sendByOP.expedition.models.entities.Vol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAnnulationTrajetRepository extends JpaRepository<AnnulationTrajet, Integer> {

    public Optional<AnnulationTrajet> findByIdtrajet(Vol vol);

}
