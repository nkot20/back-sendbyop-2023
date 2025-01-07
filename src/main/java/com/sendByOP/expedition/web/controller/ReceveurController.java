package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Receveur;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.ReceveurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ReceveurController {

    @Autowired
    ReceveurService receveurService;

    @PostMapping(value = "receveur/save")
    public ResponseEntity<?> save(@RequestBody Receveur receveur){
        Receveur newReceveur = receveurService.save(receveur);

        if (receveur == null) {
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(newReceveur, HttpStatus.CREATED);
    }

}
