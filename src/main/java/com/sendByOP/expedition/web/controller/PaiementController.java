package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Paiement;
import com.sendByOP.expedition.models.entities.Reservation;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.Clientservice;
import com.sendByOP.expedition.services.servicesImpl.PaiementService;
import com.sendByOP.expedition.services.servicesImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PaiementController {

    @Autowired
    PaiementService paiementService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    Clientservice clientservice;

    @GetMapping(value = "paiement/get")
    public ResponseEntity<?> getAll() {
        List<Paiement> paiements = paiementService.getAll();

        if(paiements == null) {
            return new ResponseEntity<>(new ResponseMessage("Aucun virement"), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(paiements, HttpStatus.OK);
    }

    @GetMapping(value = "paiement/get/{email}")
    public ResponseEntity<?> getByClient(@PathVariable("email") String email) throws SendByOpException {

        try {
            Client client = clientservice.getCustomerByEmail(email);
            List<Paiement> paiements = paiementService.getByClient(client);

            if(paiements == null) {
                return new ResponseEntity<>(new ResponseMessage("Aucun virement"), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(paiements, HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @PostMapping(value = "paiement/reservation/{id}")
    public ResponseEntity<?> payment(@PathVariable("id") int id, @RequestBody Paiement paiement) throws SendByOpException {
        try {
            Reservation reservation = reservationService.getReservation(id);

            reservation = paiementService.calculMontantFacture(id);
/*
        if(newPaiement == null) {
            return new ResponseEntity<>(new ResponseMessage("Une erreur est survenue au niveau du serveur"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        reservation.setStatutPayement(1);

        Reservation newReservation = reservationService.updateReservation(reservation);*/

            return new ResponseEntity<>(reservation, HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }

    }

}
