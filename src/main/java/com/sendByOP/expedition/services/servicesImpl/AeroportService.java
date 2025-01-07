package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.services.iServices.IAeroport;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Aeroport;
import com.sendByOP.expedition.repositories.IAeroportRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class AeroportService implements IAeroport {

    @Autowired
    IAeroportRepository aeroportRepository;

    @Override
    public Aeroport saveAeroPort(Aeroport aeroport) throws SendByOpException {
        CHeckNull.checkIntitule(aeroport.getNom());
        return aeroportRepository.save(aeroport);
    }

    @Override
    public List<Aeroport> getAllAeroports()  {
        return aeroportRepository.findAll();
    }

    @Override
    public Aeroport getAirport(int id) throws SendByOpException {
        return aeroportRepository.findByIdaero(id).orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }
}
