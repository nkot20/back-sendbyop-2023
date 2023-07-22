package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Colis;
import com.sendByOP.expedition.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColisRepository extends JpaRepository<Colis, Integer> {
    public List<Colis> findByIdre(Reservation idRe);
}
