package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.model.Pays;
import com.sendByOP.expedition.repositories.PaysRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class PaysService {

    @Autowired
    private PaysRepository paysRepository;

    public Pays saveCountry(Pays pays){
        return paysRepository.save(pays);
    }

    public List<Pays> getCountry(){
        return paysRepository.findAll();
    }

}
