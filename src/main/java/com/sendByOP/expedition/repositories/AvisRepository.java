package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Avis;
import com.sendByOP.expedition.model.Client;
import com.sendByOP.expedition.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Integer> {


    public List<Avis> findByTransporteurOrderByDateAsc(Client transporteur);

    public List<Avis> findByExpediteurOrderByDateAsc(Client transporteur);
}
