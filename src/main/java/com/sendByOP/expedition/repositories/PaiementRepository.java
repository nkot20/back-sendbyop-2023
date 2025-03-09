package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByClient(CustomerDto client);
}
