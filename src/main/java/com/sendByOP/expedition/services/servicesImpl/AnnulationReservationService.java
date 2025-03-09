package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.CancellationReservationMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.CancellationReservationDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.services.iServices.IAnnulationReservationService;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.CancellationReservation;
import com.sendByOP.expedition.repositories.IAnnulationReservationRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AnnulationReservationService implements IAnnulationReservationService {

    private final IAnnulationReservationRepository annulationReservationRepository;
    private final IReservationService reservationService;
    private final CancellationReservationMapper annulationReservationMapper;
    private final BookingMapper reservationMapper;

    @Override
    public CancellationReservationDto save(CancellationReservationDto annulationReservation) throws SendByOpException {
        CHeckNull.checkIntitule(annulationReservation.getMotif());
        return annulationReservationMapper.toDto(
                annulationReservationRepository.save(
                        annulationReservationMapper.toEntity(annulationReservation)
                )
        );
    }

    @Override
    public BookingDto saveWithReservation(CancellationReservationDto annulationReservation) throws SendByOpException {
        CancellationReservationDto annulationReservation1 = save(annulationReservation);
        BookingDto reservation = annulationReservation.getIdreservation();
        reservation.setAnnuler(1);
        return reservationService.updateReservation(reservation);
    }

    @Override
    public void delete(CancellationReservationDto annulationReservation) {
        annulationReservationRepository.deleteById(annulationReservation.getIdAnnulation());
    }

    @Override
    public CancellationReservationDto findByReservation(BookingDto id) throws SendByOpException {
        CancellationReservation annulationReservation = annulationReservationRepository.
                findByIdreservation(reservationMapper.toEntity(id))
                .orElseThrow(()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
        return annulationReservationMapper.toDto(annulationReservation);
    }

}
