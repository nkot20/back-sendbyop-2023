package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.models.dto.StopoverDto;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.services.iServices.IStopoverService;
import com.sendByOP.expedition.services.servicesImpl.VolService;
import com.sendByOP.expedition.web.exceptions.escale.ImpossibleEffectuerEscaleException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/stopover")
@RequiredArgsConstructor
public class EscaleController {

    private final IStopoverService escaleService;
    private final VolService volService;

    // Ajouter une escale
    @PostMapping(value = "/vols/save")
    public ResponseEntity<StopoverDto> ajouterUnEscale(@RequestBody StopoverDto escale) {
        StopoverDto newEscale = escaleService.addStopover(escale);
        if (newEscale == null) {
            throw new ImpossibleEffectuerEscaleException(ErrorInfo.INTRERNAL_ERROR.getMessage());
        }
        return new ResponseEntity<>(newEscale, HttpStatus.CREATED);
    }

    // Supprimer une escale
    @PostMapping(value = "/vols/delete")
    public ResponseEntity<?> supprimerEscale(@RequestBody Integer id) {
        escaleService.deleteStopover(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Liste des escales
    @GetMapping(value = "/vols/{id}")
    public ResponseEntity<List<StopoverDto>> escalList(@PathVariable("id") int id) {
        Optional<Flight> vol = volService.getVolById(id);
        if (!vol.isPresent()) {
            throw new ImpossibleEffectuerEscaleException("Vol non trouvé pour l'ID : " + id);
        }
        List<StopoverDto> escales = escaleService.findByFlightId(vol.get().getFlightId());
        return new ResponseEntity<>(escales, HttpStatus.OK);
    }
}
