package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.CancellationTripMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.mappers.FlightMapper;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.repositories.CancelTripRepository;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.exception.ErrorInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CancelReservationService {

    private final CancelTripRepository annulationTrajetRepository;
    private final ReservationService reservationService;
    private final VolService volService;
    private final FlightMapper volMapper;
    private final CancellationTripMapper annulationTrajetMapper;
    private final SendMailService sendMailService;
    private final RefundableBookingService reservationsARembourserService;
    private final BookingMapper reservationMapper;

    public CancellationReservationDto save(CancellationTripDto cancellationTripDto) throws SendByOpException {
        if (annulationTrajetDto == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
        }
        try {
            CancellationTrip annulationTrajet = annulationTrajetMapper.toEntity(cancellationTripDto);
            CancellationTrip savedAnnulationTrajet = annulationTrajetRepository.save(annulationTrajet);
            log.info("Successfully saved cancellation request for trip {}", cancellationTripDto.getTripId());
            return annulationTrajetMapper.toDto(savedAnnulationTrajet);
        } catch (Exception e) {
            log.error("Error saving cancellation request: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

    public FlightDto annulerTrajet(CancellationReservationDto annulationTrajetDto) throws SendByOpException {
        if (annulationTrajetDto == null || annulationTrajetDto.getTripId() == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
        }

        try {
            CancellationTrip annulationTrajet = annulationTrajetMapper.toEntity(annulationTrajetDto);
            CancellationReservationDto newAnnulationTrajetDto = save(annulationTrajetDto);

            int idVol = annulationTrajetDto.getTripId();
            FlightDto flightDto = volService.getVolById(idVol);
            if (flightDto == null) {
                throw new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND);
            }

            flightDto.setCancelled(1);
            FlightDto updatedVolDto = volService.updateVol(flightDto);
            if (updatedVolDto == null) {
                delete(newAnnulationTrajetDto);
                throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
            }

            List<BookingDto> reservations = reservationService.reservationList();
            reservations.removeIf(reservation -> 
                Objects.equals(reservation.getVol().getIdvol(), updatedVolDto.getIdvol()) || 
                reservation.getStatutPayement() == 1
            );

            List<EmailDto> emails = new ArrayList<>();
            log.info("Processing cancellation for flight {}", idVol);

        for (BookingDto reservation : reservations) {
            RefundableBooking reservationsARembourser = new RefundableBooking();
            reservationsARembourser.setReservation(reservationMapper.toEntity(reservation));
            reservationsARembourser.setValider(0);
            reservationsARembourserService.save(reservationsARembourser);

            EmailDto email = new EmailDto();
            email.setBody("La réservation que vous avez effectuée le "
                    + reservation.getDatere() + " a été annulée par l'expéditeur pour cause de "
                    + annulationTrajet.getMotif() + ". Un remboursement vous sera effectué.");
            emails.add(email);
        }

        sendMailService.sendListEmail(emails);

        return updatedVolDto;
    }

    public void delete(CancellationReservationDto annulationTrajetDto) throws SendByOpException {
        if (annulationTrajetDto == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
        }
        try {
            CancellationTrip annulationTrajet = annulationTrajetMapper.toEntity(annulationTrajetDto);
            annulationTrajetRepository.delete(annulationTrajet);
            log.info("Successfully deleted cancellation request for trip {}", annulationTrajetDto.getTripId());
        } catch (Exception e) {
            log.error("Error deleting cancellation request: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

    public CancellationReservationDto findByVol(FlightDto volDto) throws SendByOpException {
        if (volDto == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
        }
        try {
            Flight vol = volMapper.toEntity(volDto);
            CancellationTrip annulationTrajet = annulationTrajetRepository.findByIdtrajet(vol)
                    .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
            log.info("Found cancellation request for flight {}", volDto.getIdvol());
            return annulationTrajetMapper.toDto(annulationTrajet);
        } catch (SendByOpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding cancellation request: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

    public CancellationReservationDto update(CancellationReservationDto annulationTrajetDto) throws SendByOpException {
        if (annulationTrajetDto == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
        }
        try {
            CancellationTrip annulationTrajet = annulationTrajetMapper.toEntity(annulationTrajetDto);
            CancellationTrip updatedAnnulationTrajet = annulationTrajetRepository.save(annulationTrajet);
            log.info("Successfully updated cancellation request for trip {}", annulationTrajetDto.getTripId());
            return annulationTrajetMapper.toDto(updatedAnnulationTrajet);
        } catch (Exception e) {
            log.error("Error updating cancellation request: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }
}