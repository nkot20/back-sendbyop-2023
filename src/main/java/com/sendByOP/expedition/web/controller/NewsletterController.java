package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Newsletter;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.NewslettrerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NewsletterController {

    @Autowired
    NewslettrerService newslettrerService;

    @PostMapping("api/v1/newsletter/save")
    public ResponseEntity<?> save(@RequestBody Newsletter newsletter) {

        if(newslettrerService.getNewsLetterByEmail(newsletter.getEmail()).isPresent()) {
            return new ResponseEntity<>(new ResponseMessage("Vous ête déja inscrit dans notre newletter"), HttpStatus.FOUND);
        }

        Newsletter newsletter1 = newslettrerService.save(newsletter);

        if (newsletter == null) {
            return new ResponseEntity<>(new ResponseMessage("Une problème est survenu veuillez réessayer plutard"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(newsletter, HttpStatus.CREATED);
    }

    @PostMapping("api/v1/newsletter/get")
    public ResponseEntity<?> getAll(@RequestBody Newsletter newsletter) {
        List<Newsletter> newsletters = newslettrerService.getAll();

        return new ResponseEntity<>(newsletters, HttpStatus.OK);
    }

}
