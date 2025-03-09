package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Payment;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.services.iServices.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaiementController {

    private final IPaymentService paiementService;

    @GetMapping("/")
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> paiements = paiementService.getAll();
        return ResponseEntity.ok(paiements);
    }

    @GetMapping("/{email}")
    public ResponseEntity<List<Payment>> getPaymentsByClient(@PathVariable("email") String email) throws SendByOpException {
        List<Payment> paiements = paiementService.getPaymentsByClient(email);
        return ResponseEntity.ok(paiements);
    }

    @PostMapping("/reservation/{id}")
    public ResponseEntity<Booking> processPayment(@PathVariable("id") int id, @RequestBody Payment paiement) throws SendByOpException {
        Booking reservation = paiementService.processPayment(id, paiement);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

}
