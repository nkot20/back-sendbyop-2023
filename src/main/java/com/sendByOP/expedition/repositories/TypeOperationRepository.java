package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Typeoperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeOperationRepository extends JpaRepository<Typeoperation, Integer> {

    public Optional<Typeoperation> findByIdtypeoperation(int idTypeOperation);

}
