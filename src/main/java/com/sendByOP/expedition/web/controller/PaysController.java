package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Country;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.iServices.ICountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/country")
public class PaysController {

    private final ICountryService paysService;

    @PostMapping(value = "/save")
    public ResponseEntity<?> saveCountry(@RequestBody Country pays) {
        Country country = paysService.saveCountry(pays);

        if(country == null) {
            return new ResponseEntity<>(new ResponseMessage("Unn problème est survenu"), HttpStatus.OK);
        }

        return new ResponseEntity<>(country, HttpStatus.CREATED);
    }

    @GetMapping(value = "/")
    public ResponseEntity<?> getAllCountry() {
        return new ResponseEntity<>(paysService.getCountry(), HttpStatus.OK);
    }
}
