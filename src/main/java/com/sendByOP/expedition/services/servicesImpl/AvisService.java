package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Avis;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.repositories.AvisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class AvisService {

    @Autowired
    AvisRepository avisRepository;

    /*public List<Avis> findByReservation(Reservation  reservation ) {
        return avisRepository.findByReservation(reservation);
    }*/

    public List<Avis> findByTransporteur(Client transporteur) {
        return avisRepository.findByTransporteurOrderByDateAsc(transporteur);
    }

    public List<Avis> findByExpeditor(Client transporteur) {
        return avisRepository.findByExpediteurOrderByDateAsc(transporteur);
    }

    public Avis save(Avis avis) {
        return avisRepository.save(avis);
    }

}
