package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignalementRepository extends JpaRepository<Signalement, Integer> {
}
