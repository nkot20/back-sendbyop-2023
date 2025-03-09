package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.BankInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfoBancaireRepository extends JpaRepository<BankInfo, Integer> {

    public Optional<BankInfo> findInfoBancaireByIdclient(Customer idclient);
}
