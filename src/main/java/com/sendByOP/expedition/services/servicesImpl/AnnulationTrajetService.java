package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.model.*;
import com.sendByOP.expedition.repositories.IAnnulationTrajetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AnnulationTrajetService {

    @Autowired
    IAnnulationTrajetRepository annulationTrajetRepository;

    @Autowired
    ReservationService reservationService;
    @Autowired
    VolService volService;

    @Autowired
    SendMailService sendMailService;

    @Autowired
    ReservationsARembourserService reservationsARembourserService;

    public AnnulationTrajet save(AnnulationTrajet annulationTrajet) {
        return annulationTrajetRepository.save(annulationTrajet);
    }

    public Vol annulerTrajet(AnnulationTrajet annulationTrajet) {
        AnnulationTrajet newAnnulationTrajet = save(annulationTrajet);
        if (newAnnulationTrajet == null) return null;
        Vol vol = annulationTrajet.getIdtrajet();
        vol.setAnnuler(1);
        Vol volUpdate = volService.updateVol(vol);

        List<Reservation> reservations = reservationService.reservationList();

        reservations.removeIf(reservation -> reservation.getVol() == volUpdate);

        reservations.removeIf(reservation -> reservation.getStatutPayement() == 1);

        List<Email> emails = new ArrayList<>();

        if (volUpdate == null) {
            delete(newAnnulationTrajet);

        }

        for (Reservation reservation: reservations) {

            ReservationsARembourser reservationsARembourser = new ReservationsARembourser();
            reservationsARembourser.setReservation(reservation);
            reservationsARembourser.setValider(0);
            reservationsARembourserService.save(reservationsARembourser);

            Email email = new Email();
            email.setBody("La réservation que vous avez éffectuez le "+reservation.getDatere()+ " a été annullée par l'expéditeur pour cause de "+annulationTrajet.getMotif()+ ". Alors des remboursements vous serons éffectuez");
            emails.add(email);
        }

        sendMailService.sendListEmail(emails);

        return volUpdate;
    }

    public void delete(AnnulationTrajet annulationTrajet) {
         annulationTrajetRepository.delete(annulationTrajet);
    }

    public AnnulationTrajet findByVol(Vol vol) {
        return this.annulationTrajetRepository.findByIdtrajet(vol).get();
    }
    public AnnulationTrajet update(AnnulationTrajet annulationTrajet) {
        return this.annulationTrajetRepository.save(annulationTrajet);
    }
}
