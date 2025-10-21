package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.VerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface VerifyTokenRepository extends JpaRepository<VerifyToken, Integer> {

    VerifyToken findByToken(String token);
    
    Optional<VerifyToken> findByEmail(String email);
    
    @Modifying
    @Query("DELETE FROM VerifyToken v WHERE v.expiratedToken < :currentDate")
    void deleteExpiredTokens(@Param("currentDate") Date currentDate);
    
    @Query("SELECT COUNT(v) FROM VerifyToken v WHERE v.expiratedToken < :currentDate")
    long countExpiredTokens(@Param("currentDate") Date currentDate);

}
