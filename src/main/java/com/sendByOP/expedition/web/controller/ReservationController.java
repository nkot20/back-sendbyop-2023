package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.AnnulationReservation;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Refus;
import com.sendByOP.expedition.models.entities.Reservation;
import com.sendByOP.expedition.services.iServices.IAnnulationReservationService;
import com.sendByOP.expedition.services.iServices.IClientServivce;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
public class ReservationController {

    @Autowired
    IReservationService reservationService;

    @Autowired
    RefusService refusService;

    @Autowired
    PaiementService paiementService;

    @Autowired
    IClientServivce clientservice;

    @Autowired
    private SendMailService sendEmailService;

    @Autowired
    private IAnnulationReservationService annulationReservationService;


    /**
     * Effectuer une réservation
     * @param reservation
     * @return
     * @throws SendByOpException
     */
    @PostMapping(value = "reservations/save")
    public ResponseEntity<?> addReservation(@RequestBody Reservation reservation) throws SendByOpException {
        try {
            Reservation newReservation = reservationService.saveReservationWithColis(reservation);

            if (newReservation == null) throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);

            return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }

    }

    /**
     * Modifier la réservation
     * @param reservation
     * @return
     * @throws SendByOpException
     */
    @PutMapping(value = "reservations/update")
    public ResponseEntity<?> update(@RequestBody Reservation reservation) throws SendByOpException {
        try {
            Reservation newReservation = reservationService.updateReservation(reservation);

            if (newReservation == null) throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);

            return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    /**
     * Annuler la réservation
     * @param annulationReservation
     * @return
     * @throws SendByOpException
     */
    @PostMapping(value = "reservations/annuler")
    public ResponseEntity<?> annuler(@RequestBody AnnulationReservation annulationReservation) throws SendByOpException {



        try {
            Reservation reservation = annulationReservationService.saveWithReservation(annulationReservation);
            if (reservation == null) {
                throw  new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
            }
            return new ResponseEntity<>(reservation, HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }




    }

    /**
     * Liste des réservations
     * @return
     */
    @GetMapping(value = "reservations")
    public ResponseEntity<?> getAll(){
        List<Reservation> reservations = reservationService.reservationList();
        return  new ResponseEntity<>(reservations, HttpStatus.OK);
    }

    /**
     * Liste des réservations d'un vol
     * @param id
     * @return
     */
    @GetMapping(value = "api/v1/reservations/{id}")
    public ResponseEntity<?> getAllOfFly(@PathVariable("id") int id){


        List<Reservation> allReservations = reservationService.reservationList();
        allReservations.removeIf(reservation -> reservation.getVol().getIdvol() != id);
        return  new ResponseEntity<>(allReservations, HttpStatus.OK);
    }

    /**
     * Liste des réservations d'un client destinateur
     * @param email
     * @return
     */
    @GetMapping(value = "reservations/destinator/{id}")
    public ResponseEntity<?> getAllOfCustomerDestinator(@PathVariable("id") String email){

        try {
            Client client = clientservice.getCustomerByEmail(email);

            List<Reservation> allReservations = reservationService.clientDestinatorReservationList(client);

            return  new ResponseEntity<>(allReservations, HttpStatus.OK);

        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    /**
     * Liste des réservations d'un client expediteur
     * @param email
     * @return
     */
    @GetMapping(value = "reservations/expeditor/{id}")
    public ResponseEntity<?> getAllOfCustomerDestinato(@PathVariable("id") String email){
        try {
            Client client = clientservice.getCustomerByEmail(email);

            List<Reservation> allReservations = reservationService.reservationList();

            allReservations.removeIf(reservation -> reservation.getVol().getIdclient().getEmail() != client.getEmail());

            return  new ResponseEntity<>(allReservations, HttpStatus.OK);

        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    /**
     * supprimer une réservation
     * @param id
     * @return
     * @throws SendByOpException
     */
    @GetMapping(value = "/reservations/delete/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable("id") int id) throws SendByOpException {
        try {
            Reservation reservation = reservationService.getReservation(id);

            reservationService.deleteReservation(reservation);
            return new ResponseEntity<>(new ResponseMessage("Suppresion réussie"), HttpStatus.OK);

        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    /**
     * détails d'une réservation
     * @param id
     * @return
     * @throws SendByOpException
     */
    @GetMapping(value = "reservations/details/{id}")
    public ResponseEntity<?> getReservatioins(@PathVariable("id") int id) throws SendByOpException {

        try {
            Reservation reservation = reservationService.getReservation(id);

            return new ResponseEntity<>(reservation, HttpStatus.OK);

        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }

    }


    /**
     * valider réservation (1 pour valider)
     * @param id
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws SendByOpException
     */
    @PutMapping(value = "reservations/valider/")
    public ResponseEntity<?> validerReservation(@RequestBody int id) throws MessagingException, UnsupportedEncodingException, SendByOpException {
        try {
            Reservation reservation = reservationService.getReservation(id);

            if (reservation == null){
                return new ResponseEntity<>(new ResponseMessage("Réservation introuvale!"),
                        HttpStatus.NOT_FOUND);
            }

            reservation.setStatutReExpe(1);

            Reservation newReservation = reservationService.updateReservation(reservation);

            String content = "Bonjour [[name]],<br>"

                    + "La réservation que vous avez éffectué a été valider par l'expéditeur"
                    + "Cordialement,<br>"
                    + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>";

            sendEmailService.simpleHtmlMessage(newReservation.getVol().getIdclient(), content, "Validation de réservation");
            return new ResponseEntity<>(newReservation, HttpStatus.CREATED);

        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    /**
     * valider réservation (2 pour refuser)
     * @param id
     * @param refus
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws SendByOpException
     */
    @PutMapping(value = "/reservations/refuser/{id}")
    public ResponseEntity<?> refuserReservation(@PathVariable("id") int id, @RequestBody Refus refus) throws MessagingException, UnsupportedEncodingException, SendByOpException {

      try {
          Reservation reservation = reservationService.getReservation(id);

          if (reservation == null){
              return new ResponseEntity<>(new ResponseMessage("Réservation introuvale!"),
                      HttpStatus.NOT_FOUND);
          }

          Reservation newReservation = reservationService.refuserReservation(reservation, refus);

          return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
      } catch (SendByOpException e) {
          return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
      }
    }

    /**
     * payer réservation
     * @param id
     * @return
     * @throws SendByOpException
     */
    @GetMapping(value = "/reservations/paiement/{id}")
    public ResponseEntity<?> payementReservation(@PathVariable("id") int id) throws SendByOpException {
        try {
            Reservation reservation = paiementService.calculMontantFacture(id);
            return new ResponseEntity<Reservation>(reservation, HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    /**
     * afficher le lien qui mène vers le groupe WhatsApp d'un expéditeur. Cela se fait après le paiement de la réservatoin
     * @param idReser
     * @return
     * @throws SendByOpException
     */
    @GetMapping(value = "/lien/expediteur/{id}")
    public ResponseEntity<?> getLinkExp(@PathVariable("id") int idReser) throws SendByOpException {
        try {
            Reservation reservation = reservationService.getReservation(idReser);

            if (reservation.getStatutPayement() == 0){
                return new ResponseEntity<>(new ResponseMessage("Réservation impayée!"),
                        HttpStatus.NOT_FOUND);
            }

            Client client = clientservice.getClientById(reservation.getVol().getIdclient().getIdp());

            return new ResponseEntity<>(client.getLien(),
                    HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }


    /**
     * Ecrire un avis à l'expéditeur sur une réservation
     * @param id
     * @param opinion
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws SendByOpException
     */
    @PostMapping(value = "/write/opinion/{id}")
    public ResponseEntity<?> writeOpinion(@PathVariable("id") int id, @RequestBody String opinion) throws MessagingException, UnsupportedEncodingException, SendByOpException {

        try {
            Reservation reservation = reservationService.getReservation(id);

            if (reservation.getEtatReceptionExp() == 0){
                return new ResponseEntity<>(new ResponseMessage("Colis de la réservation non recu!"),
                        HttpStatus.NOT_FOUND);
            }

            reservation.setAvisClient(opinion);

            Reservation newReservation = reservationService.updateReservation(reservation);

            if (newReservation == null) throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);

            String content = "Bonjour [[name]],<br>"

                    + "Vous venez de recevoir une opinion sur un de vos trajets: "+opinion
                    + "Cordialement,<br>"
                    + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>";

            sendEmailService.simpleHtmlMessage(newReservation.getVol().getIdclient(), content, "Avis sur une expédition de colis");

            return new ResponseEntity<>(newReservation,
                    HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    //Répondre à un avis par l'expéditeur
    @PostMapping(value = "/write/response/{id}")
    public ResponseEntity<?> writeResponse(@PathVariable("id") int id, @RequestBody String response) throws MessagingException, UnsupportedEncodingException, SendByOpException {

       try {
           Reservation reservation = reservationService.getReservation(id);

           if (reservation.getEtatReceptionExp() == 0){
               return new ResponseEntity<>(new ResponseMessage("Colis de la réservation non recu!"),
                       HttpStatus.NOT_FOUND);
           }

           reservation.setAvisClient(response);

           Reservation newReservation = reservationService.updateReservation(reservation);

           if (newReservation == null) throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);

           String content = "Bonjour [[name]],<br>"

                   + "Vous venez de recevoir une réponse de l'épéditeur sur un avis: "+response
                   + "Cordialement,<br>"
                   + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>";

           sendEmailService.simpleHtmlMessage(newReservation.getVol().getIdclient(), content, "Réponse de l'expéditeur sur votre avis");

           return new ResponseEntity<>(newReservation,
                   HttpStatus.CREATED);
       } catch (SendByOpException e) {
           return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
       }
    }


    @GetMapping(value = "reservations/annulation/get/{id}")
    public ResponseEntity<?> getAnnultionOfReservation(@PathVariable("id") int id) throws SendByOpException {
        Reservation reservation = reservationService.getReservation(id);

        if (reservation == null){
            return new ResponseEntity<>("Réservation introuvale!",
                    HttpStatus.NOT_FOUND);
        }

        try {
            AnnulationReservation annulationReservation = annulationReservationService.findByReservation(reservation);
            return new ResponseEntity<>(annulationReservation, HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.NOT_FOUND);

        }

    }

}
