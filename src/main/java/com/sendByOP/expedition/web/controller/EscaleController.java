package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.EscaleDto;
import com.sendByOP.expedition.models.entities.Vol;
import com.sendByOP.expedition.services.iServices.IEscaleService;
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
    IEscaleService escaleService;

    @Autowired
    VolService volService;

    //Ajouter un escale
    @PostMapping(value = "vols/escales/save")
    public ResponseEntity<EscaleDto> ajouterUnEscale(@RequestBody EscaleDto escale){

        EscaleDto newEscale = escaleService.addEscale(escale);

        if (newEscale == null) throw new ImpossibleEffectuerEscaleException("Impossible d'ajouter une escale veuillez réessayer");

        return new ResponseEntity<EscaleDto>(newEscale, HttpStatus.CREATED);
    }

    //Supprimer un escale
    @PostMapping(value = "/vols/escales/delete")
    public void supprimerEscale(@RequestBody Integer id){
        escaleService.deleteEscale(id);
    }

    //liste des escales
    @GetMapping(value = "api/v1/vols/escales/{id}")
    public List<EscaleDto> escalList(@PathVariable("id") int id){
        Optional<Vol> vol = volService.getVolById(id);
        //Vol newVol = vol;
        List<EscaleDto> escales = escaleService.findByIdvol(vol);
        return escales;
    }
}
