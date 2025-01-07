package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Escale;
import com.sendByOP.expedition.models.entities.Vol;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscaleRepository extends CrudRepository<Escale, Integer> {

    public List<Escale> findByIdvol(Optional<Vol> idVol);


}
