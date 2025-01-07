package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    List<Paiement> findByClient(Client client);
}
