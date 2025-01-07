package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Receveur;
import com.sendByOP.expedition.repositories.ReceveurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ReceveurService {

    @Autowired
    ReceveurRepository receveurRepository;

    public Receveur save(Receveur receveur){
        return receveurRepository.save(receveur);
    }

}
