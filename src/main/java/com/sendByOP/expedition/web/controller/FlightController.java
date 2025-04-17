package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CancellationTripDto;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import com.sendByOP.expedition.services.iServices.IVolService;
import com.sendByOP.expedition.services.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/trips")
public class FlightController {

    private final IVolService flightService;
    private final CustomerService customerService;
    private final CancelTripService cancelTripService;

    @GetMapping
    public ResponseEntity<List<FlightDto>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllVol());
    }


    @GetMapping("/valid")
    public ResponseEntity<List<FlightDto>> getValidatedFlights() {
        return ResponseEntity.ok(flightService.getAllVolValid(1));
    }


    @GetMapping("/reject")
    public ResponseEntity<List<FlightDto>> getRejectedFlights() {
        return ResponseEntity.ok(flightService.getAllVolValid(2));
    }


    @GetMapping("/pending")
    public ResponseEntity<List<FlightDto>> getPendingFlights() {
        return ResponseEntity.ok(flightService.getAllVolValid(0));
    }

    @PostMapping
    public ResponseEntity<FlightDto> createFlight(@RequestBody VolEscaleDto flightWithStopover) throws SendByOpException {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(flightService.saveVolWithEscales(flightWithStopover));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightDto> getFlightById(@PathVariable("id") int id) {
        return ResponseEntity.ok(flightService.getVolById(id));
    }

    @PostMapping("/cancel")
    public ResponseEntity<FlightDto> cancelFlight(@RequestBody CancellationTripDto cancellation) throws SendByOpException {
        return ResponseEntity.ok(cancelTripService.cancelTrip(cancellation));
    }

    @PutMapping
    public ResponseEntity<FlightDto> updateFlight(@RequestBody FlightDto flight) throws SendByOpException {
        return ResponseEntity.ok(flightService.updateVol(flight));
    }

    @PutMapping("/validate/{id}")
    public ResponseEntity<FlightDto> validateFlight(@RequestBody int status, @PathVariable("id") int flightId) throws SendByOpException {
        FlightDto flight = flightService.getVolByIdVol(flightId);
        flight.setValidationStatus(status);
        return ResponseEntity.ok(flightService.updateVol(flight));
    }


    @GetMapping("/customer/flights/count/{email}")
    public ResponseEntity<Integer> getCustomerFlightCount(@PathVariable("email") String email) throws SendByOpException {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(flightService.nbVolClient(customer));
    }

    @GetMapping("/customer/{email}/flights")
    public ResponseEntity<List<FlightDto>> getCustomerFlights(@PathVariable("email") String email) throws SendByOpException {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(flightService.getByIdClient(customer));
    }

    @PostMapping("/cancellation/details")
    public ResponseEntity<CancellationTripDto> getCancellationDetails(@RequestBody FlightDto flight) throws SendByOpException {
        return ResponseEntity.ok(cancelTripService.findByFlight(flight));
    }


    @PutMapping("/cancellation")
    public ResponseEntity<CancellationTripDto> updateCancellation(@RequestBody CancellationTripDto cancellation) throws SendByOpException {
        return ResponseEntity.ok(cancelTripService.update(cancellation));
    }

}
