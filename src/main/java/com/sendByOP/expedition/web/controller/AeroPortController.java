package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.iServices.IAeroport;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Aeroport;
import com.sendByOP.expedition.reponse.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController

public class AeroPortController {

    @Autowired
    IAeroport aeroportService;

    @PostMapping(value = "/save/aeroport")
    public ResponseEntity<?> saveAeroport(@RequestBody Aeroport aeroport) throws SendByOpException {
        try {
            Aeroport newAeroport = aeroportService.saveAeroPort(aeroport);
            if (newAeroport == null) {
                throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
            }
            return new ResponseEntity<>(newAeroport, HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "api/v1/get/aeroports")
    public ResponseEntity<?> getAllAeroPort() {
        List<Aeroport> aeroports = aeroportService.getAllAeroports();

        return new ResponseEntity<>(aeroports, HttpStatus.OK);
    }

    @GetMapping(value = "api/v1/get/aeroports/{id}")
    public ResponseEntity<?> getAirport(@PathVariable("id") int id) throws SendByOpException {
        try {
            Aeroport aeroport = aeroportService.getAirport(id);
            return new ResponseEntity<>(aeroport, HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.NOT_FOUND);
        }


    }

}
