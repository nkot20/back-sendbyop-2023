package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Remboursement;
import com.sendByOP.expedition.models.entities.Reservation;
import com.sendByOP.expedition.repositories.RemboursementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class RemboursementService {

    @Autowired
    RemboursementRepository remboursementRepository;

    public Remboursement save(Remboursement remboursement){
        return remboursementRepository.save(remboursement);
    }

    public List<Remboursement> getRemboursement() {
        return remboursementRepository.findAll();
    }

    public Remboursement findByReservation(Reservation reservation) {
        return remboursementRepository.findByReservation(reservation).get();
    }

}
