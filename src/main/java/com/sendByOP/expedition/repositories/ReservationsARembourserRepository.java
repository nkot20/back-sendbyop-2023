package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.ReservationsARembourser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationsARembourserRepository extends JpaRepository<ReservationsARembourser, Integer> {
}
