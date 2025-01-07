package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Visite;
import com.sendByOP.expedition.repositories.VisiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class VisiteService {

    @Autowired
    VisiteRepository visitesRepository;

    public Visite addVisiteur(Visite visites){
        return visitesRepository.save(visites);
    }

    public int getNbVisiteur(){
        return visitesRepository.findAll().size();
    }

}
