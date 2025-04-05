package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Refund;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.RefundService;
import com.sendByOP.expedition.services.servicesImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reimbursement")
public class RemboursementController {

    @Autowired
    RefundService remboursementService;

    @Autowired
    ReservationService reservationService;

    @PostMapping(value = "/save")
    public ResponseEntity<?> save(@RequestBody Refund remboursement){
        Refund newRemboursement = remboursementService.save(remboursement);

        if(remboursement == null) return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(remboursement, HttpStatus.CREATED);
    }

    @GetMapping(value = "/list")
    public ResponseEntity<?> get(){
        List<Refund> remboursements = remboursementService.getRefunds();

        return new ResponseEntity<>(remboursements, HttpStatus.CREATED);
    }

    @GetMapping(value = "/reservation/{id}")
    public ResponseEntity<?> getByReservation(@PathVariable("id") int id) throws SendByOpException {

        try {
            Booking reservation = reservationService.getReservation(id);

            Refund remboursement = remboursementService.findByReservation(reservation);

            return new ResponseEntity<>(remboursement, HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());

        }
    }
}
