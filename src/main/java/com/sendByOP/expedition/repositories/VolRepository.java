package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Client;
import com.sendByOP.expedition.model.Vol;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolRepository extends CrudRepository<Vol, Integer> {

    public List<Vol> findAllByOrderByDatedepartDesc();

    public List<Vol> findByIdclientOrderByDatepublicationDesc(Client idClient);

    public List<Vol> findByEtatvalidation(int etat);

    public Vol findByIdvol(int id);

}
