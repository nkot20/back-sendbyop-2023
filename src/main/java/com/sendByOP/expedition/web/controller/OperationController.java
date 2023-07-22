package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.model.Operation;
import com.sendByOP.expedition.model.Reservation;
import com.sendByOP.expedition.model.Typeoperation;
import com.sendByOP.expedition.services.servicesImpl.OperationService;
import com.sendByOP.expedition.services.servicesImpl.ReservationService;
import com.sendByOP.expedition.services.servicesImpl.TypeOPerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OperationController {

    @Autowired
    OperationService operationService;

    @Autowired
    TypeOPerationService typeOPerationService;

    @Autowired
    ReservationService reservationService;

    @PostMapping(value = "/oparations/save/{id}")
    public ResponseEntity<Operation> saveOperation(@RequestBody Operation operation, @PathVariable("id") int id) throws Exception {

        Typeoperation typeoperation = typeOPerationService.findTypeById(id);

        operation.setIdTypeOperation(typeoperation);

        Operation newOperation = operationService.saveOperation(operation);

        if (newOperation == null) throw new Exception("Problème survenu lors de l'enregistrement");

        return new ResponseEntity<Operation>(newOperation, HttpStatus.CREATED);
    }

    @PostMapping(value = "oparations/reservation/depot/expediteur/")
    public ResponseEntity<Reservation> enregistrerDepotParExpdeiteur(@RequestBody int id) throws Exception {

        Reservation newReservation = operationService.enregistrerDepotParExpdeiteur(id);

        return new ResponseEntity<Reservation>(newReservation, HttpStatus.CREATED);

    }

    @PostMapping(value = "oparations/reservation/depot/client/")
    public ResponseEntity<Reservation> enregistrerDepotParClient(@RequestBody int id) throws Exception {

        Reservation newReservation  = operationService.enregistrerDepotParClient(id);

        //Ativer le payement de l'expéditeur

        return new ResponseEntity<Reservation>(newReservation, HttpStatus.CREATED);
    }

    @GetMapping("/operations/delete/{id}")
    public void deleteOpereation(@PathVariable("id") int id) throws Exception {
 
        Operation operation = operationService.searchOperation(id);

        if (operation != null) {
            operationService.deleteOperation(operation);
        } else {
            throw new Exception("Un problème est survenu");
        }



    }



}
