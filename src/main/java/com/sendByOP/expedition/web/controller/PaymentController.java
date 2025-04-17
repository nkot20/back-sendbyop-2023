package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PaymentDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.services.iServices.IPaymentService;
import com.sendByOP.expedition.reponse.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final IPaymentService paiementService;

    @GetMapping("/")
    public ResponseEntity<?> getAllPayments() {
        try {
            List<PaymentDto> payments = paiementService.getAll();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getPaymentsByClient(@PathVariable("email") String email) {
        try {
            List<PaymentDto> payments = paiementService.getPaymentsByClient(email);
            return ResponseEntity.ok(payments);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @PostMapping("/reservation/{id}")
    public ResponseEntity<?> processPayment(@PathVariable("id") int id, @RequestBody PaymentDto payment) throws SendByOpException {
        BookingDto reservation = paiementService.processPayment(id, payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

}
