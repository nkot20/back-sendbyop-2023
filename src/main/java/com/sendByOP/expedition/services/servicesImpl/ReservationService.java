package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.ReservationRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReservationService implements IReservationService {
    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ColisService colisService;

    @Autowired
    ReceveurService receveurService;

    @Autowired
    private SendMailService sendEmailService;


    @Autowired
    RefusService refusService;

    @Override
    public Reservation saveReservation(Reservation reservation) throws SendByOpException {
       CHeckNull.checkNumero(reservation.getReserveur().getIdp());
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation saveReservationWithColis(Reservation reservation) throws SendByOpException {
        reservation.setStatutPayement(0);
        reservation.setEtatReceptionExp(0);
        reservation.setAvisClient("");
        reservation.setAvisExpediteur("");
        reservation.setEtatReceptionClient(0);
        reservation.setAnnuler(0);
        reservation.setStastutPaimentTransporteur(0);
        reservation.setStatutReExpe(0);
        Receveur receveur = receveurService.save(reservation.getReceveur());
        reservation.setReceveur(receveur);
        List<Colis> colisList = new ArrayList<Colis>();

        reservation.getColisList().forEach(colis -> {
            colis.setIdcol(null);
            colisList.add(colis);
        });
        reservation.setColisList(colisList);
        Reservation newReservation = saveReservation(reservation);


        reservation.getColisList().forEach(colis -> {
            colis.setIdre(newReservation);
            try {
                colisService.saveColis(colis);
            } catch (SendByOpException e) {
                e.printStackTrace();
            }
        });
        return newReservation;
    }

    @Override
    public Reservation updateReservation(Reservation reservation) throws SendByOpException {
        return saveReservation(reservation);
    }

    @Override
    public void deleteReservation(Reservation reservation){
        reservationRepository.delete(reservation);
    }

    @Override
    public Reservation getReservation(int id) throws SendByOpException {
        return reservationRepository.findByIdRe(id).orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }

    @Override
    public List<Reservation> reservationList(){
        return (List<Reservation>) reservationRepository.findAllByOrderByDatereDesc();
    }

    @Override
    public List<Reservation> clientDestinatorReservationList(Client idClient){
        return reservationRepository.findByReserveurOrderByDatereDesc(idClient);
    }

    @Override
    public List<Reservation> getReservationByDate(Date date) { return reservationRepository.findByDatere(date); }

    @Override
    public Reservation refuserReservation(Reservation reservation, Refus refus) throws MessagingException, UnsupportedEncodingException, SendByOpException {


        reservation.setStatutReExpe(2);

        Reservation newReservation = updateReservation(reservation);
        refus.setIdRe(reservation);
        if (newReservation != null){
            refusService.saveRefus(refus);
        } else {
            return null;
        }

        String content = "Bonjour [[name]],<br>"

                + "La réservation que vous avez éffectué a été rejettée par l'expéditeur"
                + "Cordialement,<br>"
                + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>";

        sendEmailService.simpleHtmlMessage(newReservation.getVol().getIdclient(), content, "Validation de réservation");
        return newReservation;
    }

}
