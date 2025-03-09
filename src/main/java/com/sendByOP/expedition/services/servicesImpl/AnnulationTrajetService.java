package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.CancellationTripMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.mappers.FlightMapper;
import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.repositories.ICancelTrajetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AnnulationTrajetService {

    private final ICancelTrajetRepository annulationTrajetRepository;
    private final ReservationService reservationService;
    private final VolService volService;
    private final FlightMapper volMapper;
    private final CancellationTripMapper annulationTrajetMapper;
    private final SendMailService sendMailService;
    private final ReservationsARembourserService reservationsARembourserService;
    private final BookingMapper reservationMapper;

    public CancellationReservationDto save(CancellationReservationDto annulationTrajetDto) {
        CancellationTrip annulationTrajet = annulationTrajetMapper.toEntity(annulationTrajetDto);
        CancellationTrip savedAnnulationTrajet = annulationTrajetRepository.save(annulationTrajet);
        return annulationTrajetMapper.toDto(savedAnnulationTrajet);
    }

    public FlightDto annulerTrajet(CancellationReservationDto annulationTrajetDto) {
        CancellationTrip annulationTrajet = annulationTrajetMapper.toEntity(annulationTrajetDto);
        CancellationReservationDto newAnnulationTrajetDto = save(annulationTrajetDto);
        if (newAnnulationTrajetDto == null) return null;

        FlightDto volDto = annulationTrajetDto.getIdtrajet();
        volDto.setAnnuler(1);

        FlightDto updatedVolDto = volService.updateVol(volDto);

        List<BookingDto> reservations = reservationService.reservationList();
        reservations.removeIf(reservation -> reservation.getVol().getIdvol().equals(updatedVolDto.getIdvol()));
        reservations.removeIf(reservation -> reservation.getStatutPayement() == 1);

        List<EmailDto> emails = new ArrayList<>();

        if (updatedVolDto == null) {
            delete(newAnnulationTrajetDto);
            return null;
        }

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

    public void delete(CancellationReservationDto annulationTrajetDto) {
        CancellationTrip annulationTrajet = annulationTrajetMapper.toEntity(annulationTrajetDto);
        annulationTrajetRepository.delete(annulationTrajet);
    }

    public CancellationReservationDto findByVol(FlightDto volDto) {
        Flight vol = volMapper.toEntity(volDto);
        CancellationTrip annulationTrajet = annulationTrajetRepository.findByIdtrajet(vol)
                .orElseThrow(() -> new RuntimeException("AnnulationTrajet not found"));
        return annulationTrajetMapper.toDto(annulationTrajet);
    }

    public CancellationReservationDto update(CancellationReservationDto annulationTrajetDto) {
        CancellationTrip annulationTrajet = annulationTrajetMapper.toEntity(annulationTrajetDto);
        CancellationTrip updatedAnnulationTrajet = annulationTrajetRepository.save(annulationTrajet);
        return annulationTrajetMapper.toDto(updatedAnnulationTrajet);
    }
}