package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Stopover;
import com.sendByOP.expedition.models.entities.Flight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopoverRepository extends CrudRepository<Stopover, Integer> {

    public List<Stopover> findByFlight(Flight flight);


}
