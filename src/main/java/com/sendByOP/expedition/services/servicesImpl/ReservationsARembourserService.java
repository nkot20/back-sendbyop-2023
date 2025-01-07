package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.ReservationsARembourser;
import com.sendByOP.expedition.repositories.ReservationsARembourserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ReservationsARembourserService {

    @Autowired
    ReservationsARembourserRepository reservationsARembourserRepository;

    public ReservationsARembourser save(ReservationsARembourser reservationsARembourser) {
        return reservationsARembourserRepository.save(reservationsARembourser);
    }

    public List<ReservationsARembourser> findAll() {
        return reservationsARembourserRepository.findAll();
    }

}
