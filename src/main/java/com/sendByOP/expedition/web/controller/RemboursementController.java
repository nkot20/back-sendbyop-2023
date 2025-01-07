package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Remboursement;
import com.sendByOP.expedition.models.entities.Reservation;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.RemboursementService;
import com.sendByOP.expedition.services.servicesImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RemboursementController {

    @Autowired
    RemboursementService remboursementService;

    @Autowired
    ReservationService reservationService;

    @PostMapping(value = "remboursements/save")
    public ResponseEntity<?> save(@RequestBody Remboursement remboursement){
        Remboursement newRemboursement = remboursementService.save(remboursement);

        if(remboursement == null) return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(remboursement, HttpStatus.CREATED);
    }

    @GetMapping(value = "remboursements/list")
    public ResponseEntity<?> get(){
        List<Remboursement> remboursements = remboursementService.getRemboursement();

        return new ResponseEntity<>(remboursements, HttpStatus.CREATED);
    }

    @GetMapping(value = "remboursements/get/reservation/{id}")
    public ResponseEntity<?> getByReservation(@PathVariable("id") int id) throws SendByOpException {

        try {
            Reservation reservation = reservationService.getReservation(id);

            Remboursement remboursement = remboursementService.findByReservation(reservation);

            return new ResponseEntity<>(remboursement, HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());

        }
    }
}
