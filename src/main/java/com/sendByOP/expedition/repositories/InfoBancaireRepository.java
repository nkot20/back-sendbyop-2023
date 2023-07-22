package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Client;
import com.sendByOP.expedition.model.InfoBancaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfoBancaireRepository extends JpaRepository<InfoBancaire, Integer> {

    public Optional<InfoBancaire> findInfoBancaireByIdclient(Client idclient);
}
