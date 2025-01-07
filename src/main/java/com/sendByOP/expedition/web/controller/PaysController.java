package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Pays;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.PaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaysController {

    @Autowired
    PaysService paysService;

    @PostMapping(value = "country/save")
    public ResponseEntity<?> saveCountry(@RequestBody Pays pays) {
        Pays country = paysService.saveCountry(pays);

        if(country == null) {
            return new ResponseEntity<>(new ResponseMessage("Unn problème est survenu"), HttpStatus.OK);
        }

        return new ResponseEntity<>(country, HttpStatus.CREATED);
    }

    @GetMapping(value = "api/v1/country/getall")
    public ResponseEntity<?> getAllCountry() {
        return new ResponseEntity<>(paysService.getCountry(), HttpStatus.OK);
    }
}
