package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Receiver;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.ReceveurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/receiver")
public class ReceveurController {

    @Autowired
    ReceveurService receveurService;

    @PostMapping(value = "/save")
    public ResponseEntity<?> save(@RequestBody Receiver receveur){
        Receiver newReceveur = receveurService.save(receveur);

        if (receveur == null) {
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(newReceveur, HttpStatus.CREATED);
    }

}
