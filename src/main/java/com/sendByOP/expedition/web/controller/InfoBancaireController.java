package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BankInfoDto;
import com.sendByOP.expedition.services.iServices.IInfoBancaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bankinfos")
public class InfoBancaireController {

    private final IInfoBancaireService iInfoBancaireService;

    @PostMapping("/save")
    public ResponseEntity<?> saveInfo(@RequestBody BankInfoDto infoBancaire) throws SendByOpException {
        return new ResponseEntity<>(iInfoBancaireService.save(infoBancaire), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInfoIban(@PathVariable("id") int id) throws SendByOpException {
        return new ResponseEntity<>(iInfoBancaireService.getInfoBancaire(id), HttpStatus.CREATED);
    }

}
