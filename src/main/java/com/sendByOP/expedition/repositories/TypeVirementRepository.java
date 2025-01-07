package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.TypeVirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeVirementRepository extends JpaRepository<TypeVirement, Integer> {
}
