package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.RefundableBooking;
import com.sendByOP.expedition.repositories.ReservationsARembourserRepository;
import com.sendByOP.expedition.services.iServices.IReservationsARembourserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationsARembourserService implements IReservationsARembourserService {

    private final ReservationsARembourserRepository reservationsARembourserRepository;

    @Override
    public RefundableBooking save(RefundableBooking reservationsARembourser) {
        return reservationsARembourserRepository.save(reservationsARembourser);
    }

    @Override
    public List<RefundableBooking> findAll() {
        return reservationsARembourserRepository.findAll();
    }

}
