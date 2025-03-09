package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Refund;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.repositories.RemboursementRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
public class RemboursementService {

    @Autowired
    RemboursementRepository remboursementRepository;

    public Refund save(Refund remboursement){
        return remboursementRepository.save(remboursement);
    }

    public List<Refund> getRemboursement() {
        return remboursementRepository.findAll();
    }

    public Refund findByReservation(Booking reservation) {
        return remboursementRepository.findByReservation(reservation).get();
    }

}
