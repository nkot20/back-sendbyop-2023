package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeVirementRepository extends JpaRepository<PaymentType, Integer> {
}
