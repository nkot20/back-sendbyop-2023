package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Visite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisiteRepository extends JpaRepository<Visite, Integer> {
}
