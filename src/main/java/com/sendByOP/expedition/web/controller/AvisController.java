package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Avis;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.AvisService;
import com.sendByOP.expedition.services.servicesImpl.Clientservice;
import com.sendByOP.expedition.services.servicesImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AvisController {

    @Autowired
    public AvisService avisService;

    @Autowired
    public ReservationService reservationService;

    @Autowired
    public Clientservice clientservice;

    @PostMapping("avis/save")
    public ResponseEntity<?> saveOpinion(@RequestBody Avis avis) {
        Avis newOpinion = avisService.save(avis);

        if(avis == null) {
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu veuillez réssayer plutard"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(avis, HttpStatus.CREATED);
    }

    /*@GetMapping("api/v1/avis/reservation/{id}")
    public ResponseEntity<?> getByReservation(@PathVariable("id") int idReservation) {

        Reservation reservation = reservationService.getReservation(idReservation);

        if(reservation == null) {
            return new ResponseEntity<>(new ResponseMessage("Réservation introuvable"), HttpStatus.NOT_FOUND);
        }

        List<Avis> avisList = avisService.findByReservation(reservation);

        if (avisList == null && avisList.size() == 0) {
            return new ResponseEntity<>(new ResponseMessage("Aucun avis pour cette réservation"), HttpStatus.OK);
        }

        return new ResponseEntity<>(avisList, HttpStatus.OK);

    }*/

    @GetMapping("api/v1/avis/transporteur/{id}")
    public ResponseEntity<?> getByTransporter(@PathVariable("id") int idTransporter) {

        Client transporter = clientservice.getClientById(idTransporter);

        if(transporter == null) {
            return new ResponseEntity<>(new ResponseMessage("Traansporteur introuvable"), HttpStatus.NOT_FOUND);
        }

        List<Avis> avisList = avisService.findByTransporteur(transporter);

        if (avisList == null && avisList.size() == 0) {
            return new ResponseEntity<>(new ResponseMessage("Aucun avis pour cette réservation"), HttpStatus.OK);
        }

        return new ResponseEntity<>(avisList, HttpStatus.OK);

    }

    @GetMapping("api/v1/avis/expediteur/{id}")
    public ResponseEntity<?> getByExpeditor(@PathVariable("id") int idTransporter) {

        Client transporter = clientservice.getClientById(idTransporter);

        if(transporter == null) {
            return new ResponseEntity<>(new ResponseMessage("Transporteur introuvable"), HttpStatus.NOT_FOUND);
        }

        List<Avis> avisList = avisService.findByExpeditor(transporter);

        if (avisList == null && avisList.size() == 0) {
            return new ResponseEntity<>(new ResponseMessage("Aucun avis pour cette réservation"), HttpStatus.OK);
        }

        return new ResponseEntity<>(avisList, HttpStatus.OK);

    }

}
