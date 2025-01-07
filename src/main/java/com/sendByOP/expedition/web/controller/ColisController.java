package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.iServices.IColisService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Colis;
import com.sendByOP.expedition.models.entities.Reservation;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ColisController {

    @Autowired
    IColisService colisService;

    @Autowired
    ReservationService reservationService;

    @PostMapping(value = "colis/save")
    public ResponseEntity<Colis> saveColis(@RequestBody Colis colis) throws Exception {

        Colis newColis = colisService.saveColis(colis);

        if (newColis == null) throw new Exception("Impossible d'ajouter les colis");

        return new ResponseEntity<Colis>(newColis, HttpStatus.CREATED);

    }

    @PostMapping(value = "colis/delete")
    public void deleteColis(@RequestBody Colis colis){
        colisService.deleteColis(colis);
    }

    @GetMapping("reservations/colis/{id}")
    public ResponseEntity<?> getColisForReservation(@PathVariable("id") int id) throws SendByOpException {
        try {
            Reservation reservation = reservationService.getReservation(id);
            return new ResponseEntity<>(colisService.findAllColisByForReservation(reservation), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

}
