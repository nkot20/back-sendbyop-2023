package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.services.iServices.IAeroport;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Airport;
import com.sendByOP.expedition.repositories.IAeroportRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AeroportService implements IAeroport {

    private final IAeroportRepository aeroportRepository;

    @Override
    public Airport saveAeroPort(Airport aeroport) throws SendByOpException {
        CHeckNull.checkIntitule(aeroport.getNom());
        return aeroportRepository.save(aeroport);
    }

    @Override
    public List<Airport> getAllAeroports()  {
        return aeroportRepository.findAll();
    }

    @Override
    public Airport getAirport(int id) throws SendByOpException {
        return aeroportRepository.findByIdaero(id).orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }
}
