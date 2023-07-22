package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.model.Visite;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.VisiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VisiteurController {

    @Autowired
    VisiteService  visiteService;

    @PostMapping(value = "api/v1/visites/add")
    public ResponseEntity<?> addVisitor(@RequestBody Visite visite){
        visite.setId(null);
        Visite newVisitor = visiteService.addVisiteur(visite);

        if(newVisitor == null){
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu!"),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new ResponseMessage("Enregistrement réussi!"), HttpStatus.OK);
    }

}
