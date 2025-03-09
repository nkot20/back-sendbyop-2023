package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Operation;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.services.iServices.IOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operation")
@RequiredArgsConstructor
public class OperationController {

    private final IOperationService operationService;

    @PostMapping(value = "/save/{id}")
    public ResponseEntity<Operation> saveOperation(@RequestBody Operation operation, @PathVariable("id") int id) {
        Operation newOperation = operationService.saveOperation(operation, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOperation);
    }

    @PostMapping(value = "/reservation/depot/expediteur/")
    public ResponseEntity<Booking> enregistrerDepotParExpediteur(@RequestBody int id) throws Exception {
        Booking newReservation = operationService.enregistrerDepotParExpediteur(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
    }

    @PostMapping(value = "/reservation/depot/client/")
    public ResponseEntity<Booking> enregistrerDepotParClient(@RequestBody int id) throws Exception {
        Booking newReservation = operationService.saveDepotParClient(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable("id") int id) {
        operationService.deleteOperation(id);
        return ResponseEntity.noContent().build();
    }



}
