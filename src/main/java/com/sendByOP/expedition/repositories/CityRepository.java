package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.City;
import com.sendByOP.expedition.models.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    Optional<City> findByNameAndCountry(String name, Country country);
}
