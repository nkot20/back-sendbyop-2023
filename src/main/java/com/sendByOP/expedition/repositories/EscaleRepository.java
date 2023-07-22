package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Escale;
import com.sendByOP.expedition.model.Vol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscaleRepository extends CrudRepository<Escale, Integer> {

    public List<Escale> findByIdvol(Optional<Vol> idVol);


}
