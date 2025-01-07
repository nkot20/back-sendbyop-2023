package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Receveur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceveurRepository extends JpaRepository<Receveur, Integer> {
}
