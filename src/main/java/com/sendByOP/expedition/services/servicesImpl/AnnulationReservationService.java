package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.services.IServices.IAnnulationReservationService;
import com.sendByOP.expedition.services.IServices.IReservationService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.AnnulationReservation;
import com.sendByOP.expedition.model.Reservation;
import com.sendByOP.expedition.repositories.IAnnulationReservationRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class AnnulationReservationService implements IAnnulationReservationService {

    @Autowired
    IAnnulationReservationRepository annulationReservationRepository;

    @Autowired
    IReservationService reservationService;

    @Override
    public AnnulationReservation save(AnnulationReservation annulationReservation) throws SendByOpException {
        CHeckNull.checkIntitule(annulationReservation.getMotif());
        return annulationReservationRepository.save(annulationReservation);
    }

    @Override
    public Reservation saveWithReservation(AnnulationReservation annulationReservation) throws SendByOpException {
        AnnulationReservation annulationReservation1 = save(annulationReservation);
        Reservation reservation = annulationReservation.getIdreservation();
        reservation.setAnnuler(1);
        return reservationService.updateReservation(reservation);
    }

    @Override
    public void delete(AnnulationReservation annulationReservation) {
        annulationReservationRepository.delete(annulationReservation);
    }

    @Override
    public AnnulationReservation findByReservation(Reservation id) throws SendByOpException {
        return annulationReservationRepository.findByIdreservation(id).orElseThrow(()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }

}
