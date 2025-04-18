package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Rejection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RejectionRepository extends JpaRepository<Rejection, Integer> {
}
