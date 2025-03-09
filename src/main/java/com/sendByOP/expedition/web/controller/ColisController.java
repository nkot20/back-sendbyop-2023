package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.services.iServices.IColisService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Parcel;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.services.servicesImpl.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/package")
@RequiredArgsConstructor
public class ColisController {
    private final IColisService colisService;
    private final ReservationService reservationService;

    @PostMapping(value = "/save")
    public ResponseEntity<Parcel> saveColis(@Valid @RequestBody Parcel colis) throws SendByOpException {
        Parcel newColis = colisService.saveColis(colis);
        if (newColis == null) {
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
        return new ResponseEntity<>(newColis, HttpStatus.CREATED);
    }

    @PostMapping(value = "/delete")
    public ResponseEntity<?> deleteColis(@RequestBody Parcel colis) {
        colisService.deleteColis(colis);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<?> getColisForReservation(@PathVariable("id") int id) throws SendByOpException {
        Booking reservation = reservationService.getReservation(id);
        if (reservation == null) {
            throw new SendByOpException(ErrorInfo.UNEXPECTED_ERROR);
        }
        return new ResponseEntity<>(colisService.findAllColisByForReservation(reservation), HttpStatus.OK);
    }
}
