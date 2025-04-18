package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.OperationDto;
import com.sendByOP.expedition.models.dto.BookingDto;
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

    @PostMapping(value = "/save/{typeId}")
    public ResponseEntity<OperationDto> saveOperation(@RequestBody OperationDto operation, @PathVariable("typeId") int typeId) throws SendByOpException {
        OperationDto newOperation = operationService.saveOperation(operation, typeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOperation);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationDto> getOperation(@PathVariable("id") int id) throws SendByOpException {
        OperationDto operation = operationService.searchOperation(id);
        return ResponseEntity.ok(operation);
    }

    @PostMapping(value = "/booking/deposit/sender")
    public ResponseEntity<BookingDto> registerSenderDeposit(@RequestBody int id) throws Exception {
        BookingDto newBooking = operationService.registerSenderDeposit(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    @PostMapping(value = "/booking/deposit/customer")
    public ResponseEntity<BookingDto> registerCustomerDeposit(@RequestBody int id) throws Exception {
        BookingDto newBooking = operationService.registerCustomerDeposit(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable("id") int id) throws SendByOpException {
        operationService.deleteOperation(id);
        return ResponseEntity.noContent().build();
    }



}
