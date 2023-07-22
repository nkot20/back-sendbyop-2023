package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.InfoBancaire;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.IServices.IInfoBancaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class InfoBancaireController {

    @Autowired
    IInfoBancaireService iInfoBancaireService;

    @PostMapping("iban/save")
    public ResponseEntity<?> saveInfo(@RequestBody InfoBancaire infoBancaire) throws SendByOpException{
        try {
            return new ResponseEntity<>(iInfoBancaireService.save(infoBancaire), HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @GetMapping("iban/get/{id}")
    public ResponseEntity<?> getInfoIban(@PathVariable("id") int id) throws SendByOpException{
        try {
            return new ResponseEntity<>(iInfoBancaireService.getInfoBancaire(id), HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

}
