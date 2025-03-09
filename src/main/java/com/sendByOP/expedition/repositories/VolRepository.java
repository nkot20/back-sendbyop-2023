package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Flight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolRepository extends CrudRepository<Flight, Integer> {

    public List<Flight> findAllByOrderByDatedepartDesc();

    public List<Flight> findByIdclientOrderByDatepublicationDesc(int idClient);

    public List<Flight> findByEtatvalidation(int etat);

    public Optional<Flight> findByIdvol(int id);

}
