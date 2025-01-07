package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Integer> {

    public List<Operation> findByIdTypeOperation(Operation idTypeOperation);

    public Optional<Operation> findByIdOpe(int id);

}
