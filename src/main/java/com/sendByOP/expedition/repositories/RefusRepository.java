package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Refus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefusRepository extends JpaRepository<Refus, Integer> {
}
