package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperationTypeRepository extends JpaRepository<OperationType, Integer> {

}
