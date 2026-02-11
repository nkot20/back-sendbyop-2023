package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirPortRepository extends JpaRepository<Airport, Integer> {
    Optional<Airport> findByIataCode(String iataCode);
    
    /**
     * Récupère tous les aéroports avec leurs relations City et Country en une seule requête
     * pour éviter les problèmes de lazy loading et les connection leaks
     */
    @Query("SELECT DISTINCT a FROM Airport a " +
           "LEFT JOIN FETCH a.city c " +
           "LEFT JOIN FETCH c.country")
    List<Airport> findAllWithCityAndCountry();
}
