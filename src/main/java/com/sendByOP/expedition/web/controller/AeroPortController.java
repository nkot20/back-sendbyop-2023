package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.services.iServices.IAeroport;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Airport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/airport")
@RequiredArgsConstructor
public class AeroPortController {
    private final IAeroport aeroportService;

    @PostMapping("/save")
    public ResponseEntity<Airport> saveAeroport(@RequestBody Airport aeroport) throws SendByOpException {
        Airport newAeroport = aeroportService.saveAeroPort(aeroport);
        if (newAeroport == null) {
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newAeroport);
    }

    @GetMapping("/")
    public ResponseEntity<List<Airport>> getAllAeroPorts() {
        List<Airport> aeroports = aeroportService.getAllAeroports();
        return ResponseEntity.ok(aeroports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Airport> getAirport(@PathVariable("id") int id) throws SendByOpException {
        Airport aeroport = aeroportService.getAirport(id);
        return ResponseEntity.ok(aeroport);
    }
}
