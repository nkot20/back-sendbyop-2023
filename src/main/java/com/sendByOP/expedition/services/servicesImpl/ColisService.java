package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.services.IServices.IColisService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.Colis;
import com.sendByOP.expedition.model.Reservation;
import com.sendByOP.expedition.repositories.ColisRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ColisService implements IColisService {

    @Autowired
    ColisRepository colisRepository;

    @Override
    public Colis saveColis(Colis colis) throws SendByOpException {
        CHeckNull.checkIntitule(colis.getDescription());
        return colisRepository.save(colis);
    }

    @Override
    public void deleteColis(Colis colis){
        colisRepository.delete(colis);
    }

    @Override
    public Colis findColis(int id) throws SendByOpException {
        return colisRepository.findById(id).orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }

    @Override
    public List<Colis> findAllColisByForReservation(Reservation idRe){ return colisRepository.findByIdre(idRe); }
}
