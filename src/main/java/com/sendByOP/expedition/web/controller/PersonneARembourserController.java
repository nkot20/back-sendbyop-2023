package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.servicesImpl.ReservationsARembourserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonneARembourserController {

    @Autowired
    ReservationsARembourserService reservationsARembourserService;

    @GetMapping(value = "reservations/remboursements")
    public ResponseEntity<?> getAll() {
        return new ResponseEntity<>(reservationsARembourserService.findAll(), HttpStatus.OK);
    }

}
