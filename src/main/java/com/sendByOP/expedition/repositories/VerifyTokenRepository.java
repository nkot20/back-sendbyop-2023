package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.VerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifyTokenRepository extends JpaRepository<VerifyToken, Integer> {

    public VerifyToken findByToken(String token);

}
