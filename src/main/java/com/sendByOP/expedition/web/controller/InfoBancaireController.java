package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.InfoBancaireDto;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.iServices.IInfoBancaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InfoBancaireController {

    private final IInfoBancaireService iInfoBancaireService;

    @PostMapping("iban/save")
    public ResponseEntity<?> saveInfo(@RequestBody InfoBancaireDto infoBancaire) {
        try {
            return new ResponseEntity<>(iInfoBancaireService.save(infoBancaire), HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @GetMapping("iban/get/{id}")
    public ResponseEntity<?> getInfoIban(@PathVariable("id") int id) {
        try {
            return new ResponseEntity<>(iInfoBancaireService.getInfoBancaire(id), HttpStatus.CREATED);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

}
