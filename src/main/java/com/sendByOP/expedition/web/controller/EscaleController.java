package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.model.Escale;
import com.sendByOP.expedition.model.Vol;
import com.sendByOP.expedition.services.servicesImpl.EscaleService;
import com.sendByOP.expedition.services.servicesImpl.VolService;
import com.sendByOP.expedition.web.exceptions.escale.ImpossibleEffectuerEscaleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class EscaleController {

    @Autowired
    EscaleService escaleService;

    @Autowired
    VolService volService;

    //Ajouter un escale
    @PostMapping(value = "vols/escales/save")
    public ResponseEntity<Escale> ajouterUnEscale(@RequestBody Escale escale){

        Escale newEscale = escaleService.addEscale(escale);

        if (newEscale == null) throw new ImpossibleEffectuerEscaleException("Impossible d'ajouter une escale veuillez réessayer");

        return new ResponseEntity<Escale>(newEscale, HttpStatus.CREATED);
    }

    //Supprimer un escale
    @PostMapping(value = "/vols/escales/delete")
    public void supprimerEscale(@RequestBody Escale escale){
        escaleService.deleteEscale(escale);
    }

    //liste des escales
    @GetMapping(value = "api/v1/vols/escales/{id}")
    public List<Escale> escalList(@PathVariable("id") int id){
        Optional<Vol> vol = volService.getVolById(id);
        //Vol newVol = vol;
        List<Escale> escales = escaleService.findByIdvol(vol);
        return escales;
    }
}
