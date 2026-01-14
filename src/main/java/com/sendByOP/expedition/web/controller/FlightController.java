package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.services.iServices.IVolService;
import com.sendByOP.expedition.services.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/trips")
@Tag(name = "Flight Management", description = "APIs for managing flights and trip operations")
@Slf4j
public class FlightController {

    private final IVolService flightService;
    private final CustomerService customerService;
    private final CancelTripService cancelTripService;

    @Operation(summary = "Get all flights", description = "Retrieves a list of all flights in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of flights")
    @GetMapping
    public ResponseEntity<List<FlightDto>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllVol());
    }


    @Operation(summary = "Get validated flights", description = "Retrieves a list of all validated flights")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved validated flights")
    @GetMapping("/valid")
    public ResponseEntity<List<FlightDto>> getValidatedFlights() {
        return ResponseEntity.ok(flightService.getAllVolValid(1));
    }


    @Operation(summary = "Get rejected flights", description = "Retrieves a list of all rejected flights")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved rejected flights")
    @GetMapping("/reject")
    public ResponseEntity<List<FlightDto>> getRejectedFlights() {
        return ResponseEntity.ok(flightService.getAllVolValid(2));
    }


    @Operation(summary = "Get pending flights", description = "Retrieves a list of all pending flights awaiting validation")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved pending flights")
    @GetMapping("/pending")
    public ResponseEntity<List<FlightDto>> getPendingFlights() {
        return ResponseEntity.ok(flightService.getAllVolValid(0));
    }

    @GetMapping("/public/active")
    @Operation(summary = "Get all valid and active flights (Public API)", 
               description = "Retrieve all flights that are validated and active for public viewing with detailed information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved valid and active flights with detailed information")
    public ResponseEntity<List<PublicFlightDto>> getValidAndActiveFlights() {
        log.debug("Public API request for valid and active flights");
        List<PublicFlightDto> flights = flightService.getPublicValidAndActiveFlights();
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/public/active/paginated")
    @Operation(summary = "Get paginated valid and active flights (Public API)", 
               description = "Retrieve paginated flights that are validated and active for public viewing with detailed information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated valid and active flights with detailed information")
    public ResponseEntity<Page<PublicFlightDto>> getValidAndActiveFlightsPaginated(
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-based)") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of items per page") int size) {
        log.debug("Public API request for paginated valid and active flights - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PublicFlightDto> flights = flightService.getPublicValidAndActiveFlights(pageable);
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Get flight details by ID (Public API)", 
               description = "Retrieve detailed information about a specific flight for public viewing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<PublicFlightDto> getPublicFlightById(@PathVariable("id") @Parameter(description = "Flight ID") int id) {
        log.debug("Public API request for flight details with id: {}", id);
        PublicFlightDto flight = flightService.getPublicFlightById(id);
        return ResponseEntity.ok(flight);
    }

    @Operation(summary = "Create a new flight", description = "Creates a new flight with optional stopovers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flight created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid flight data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @PostMapping
    public ResponseEntity<FlightDto> createFlight(@RequestBody @Parameter(description = "Flight data with optional stopovers") VolEscaleDto flightWithStopover) throws SendByOpException {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(flightService.saveVolWithEscales(flightWithStopover));
    }

    @Operation(summary = "Get flight by ID", description = "Retrieves a specific flight by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight found successfully"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FlightDto> getFlightById(@PathVariable("id") @Parameter(description = "Flight ID") int id) {
        return ResponseEntity.ok(flightService.getVolById(id));
    }

    @Operation(summary = "Cancel a flight", description = "Cancels an existing flight with cancellation details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid cancellation data"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @PostMapping("/cancel")
    public ResponseEntity<FlightDto> cancelFlight(@RequestBody @Parameter(description = "Cancellation details") CancellationTripDto cancellation) throws SendByOpException {
        return ResponseEntity.ok(cancelTripService.cancelTrip(cancellation));
    }

    @Operation(summary = "Update a flight", description = "Updates an existing flight with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid flight data"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @PutMapping
    public ResponseEntity<FlightDto> updateFlight(@RequestBody @Parameter(description = "Updated flight data") FlightDto flight) throws SendByOpException {
        return ResponseEntity.ok(flightService.updateVol(flight));
    }

    @Operation(summary = "Validate or reject a flight", description = "Updates the validation status of a flight (0: Pending, 1: Validated, 2: Rejected)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight validation status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @PutMapping("/validate/{id}")
    public ResponseEntity<FlightDto> validateFlight(@RequestBody @Parameter(description = "Validation status (0: Pending, 1: Validated, 2: Rejected)") int status, @PathVariable("id") @Parameter(description = "Flight ID") int flightId) throws SendByOpException {
        FlightDto flight = flightService.getVolByIdVol(flightId);
        flight.setValidationStatus(status);
        return ResponseEntity.ok(flightService.updateVol(flight));
    }


    @Operation(summary = "Get customer flight count", description = "Retrieves the number of validated flights for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customer/flights/count/{email}")
    public ResponseEntity<Integer> getCustomerFlightCount(@PathVariable("email") @Parameter(description = "Customer email") String email) throws SendByOpException {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(flightService.nbVolClient(customer));
    }

    @Operation(summary = "Get customer flights", description = "Retrieves all flights for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer flights retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customer/{email}/flights")
    public ResponseEntity<List<FlightDto>> getCustomerFlights(@PathVariable("email") @Parameter(description = "Customer email") String email) throws SendByOpException {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(flightService.getByIdClient(customer));
    }

    @Operation(summary = "Get cancellation details", description = "Retrieves cancellation details for a specific flight")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Flight or cancellation not found")
    })
    @PostMapping("/cancellation/details")
    public ResponseEntity<CancellationTripDto> getCancellationDetails(@RequestBody @Parameter(description = "Flight data") FlightDto flight) throws SendByOpException {
        return ResponseEntity.ok(cancelTripService.findByFlight(flight));
    }


    @Operation(summary = "Update cancellation", description = "Updates cancellation details for a flight")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid cancellation data"),
            @ApiResponse(responseCode = "404", description = "Cancellation not found")
    })
    @PutMapping("/cancellation")
    public ResponseEntity<CancellationTripDto> updateCancellation(@RequestBody @Parameter(description = "Updated cancellation data") CancellationTripDto cancellation) throws SendByOpException {
        return ResponseEntity.ok(cancelTripService.update(cancellation));
    }

    /**
     * Endpoint simplifié pour annuler un vol par ID
     * Utilisé par le frontend Angular
     * Version simplifiée qui ne passe pas par CancelTripService pour éviter les problèmes de lazy loading
     */
    @Operation(summary = "Cancel flight by ID", description = "Cancels a flight using only the flight ID with an optional reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @PutMapping("/{flightId}/cancel")
    public ResponseEntity<FlightDto> cancelFlightById(
            @PathVariable("flightId") @Parameter(description = "Flight ID to cancel") Integer flightId,
            @RequestParam(required = false, defaultValue = "Annulé par le voyageur") @Parameter(description = "Cancellation reason") String reason) throws SendByOpException {
        
        // #region agent log
        try{java.nio.file.Files.write(java.nio.file.Paths.get("d:\\SendByOp2023\\Backend\\.cursor\\debug.log"),String.format("{\"location\":\"FlightController.java:215\",\"message\":\"Entry cancelFlightById\",\"data\":{\"flightId\":%d,\"reason\":\"%s\"},\"timestamp\":%d,\"sessionId\":\"debug-session\",\"hypothesisId\":\"H1,H2,H3\"}%n",flightId,reason,System.currentTimeMillis()).getBytes(),java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);}catch(Exception e){}
        // #endregion
        
        log.info("PUT /trips/{}/cancel - Cancellation request with reason: {}", flightId, reason);
        
        // Récupérer le vol
        FlightDto flight = flightService.getVolById(flightId);
        
        // #region agent log
        try{java.nio.file.Files.write(java.nio.file.Paths.get("d:\\SendByOp2023\\Backend\\.cursor\\debug.log"),String.format("{\"location\":\"FlightController.java:220\",\"message\":\"After getVolById\",\"data\":{\"flightIsNull\":%b,\"departureAirportId\":%s,\"arrivalAirportId\":%s,\"cancelled\":%s},\"timestamp\":%d,\"sessionId\":\"debug-session\",\"hypothesisId\":\"H3\"}%n",flight==null,flight!=null?flight.getDepartureAirportId():"null",flight!=null?flight.getArrivalAirportId():"null",flight!=null?flight.getCancelled():"null",System.currentTimeMillis()).getBytes(),java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);}catch(Exception e){}
        // #endregion
        
        if (flight == null) {
            throw new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, "Vol non trouvé");
        }
        
        // Marquer comme annulé
        flight.setCancelled(1);
        
        // #region agent log
        try{java.nio.file.Files.write(java.nio.file.Paths.get("d:\\SendByOp2023\\Backend\\.cursor\\debug.log"),String.format("{\"location\":\"FlightController.java:228\",\"message\":\"Before updateVol\",\"data\":{\"flightId\":%d,\"cancelled\":%d,\"departureAirportId\":%s,\"arrivalAirportId\":%s},\"timestamp\":%d,\"sessionId\":\"debug-session\",\"hypothesisId\":\"H4,H5\"}%n",flight.getFlightId(),flight.getCancelled(),flight.getDepartureAirportId(),flight.getArrivalAirportId(),System.currentTimeMillis()).getBytes(),java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);}catch(Exception e){}
        // #endregion
        
        FlightDto cancelledFlight = flightService.updateVol(flight);
        
        // #region agent log
        try{java.nio.file.Files.write(java.nio.file.Paths.get("d:\\SendByOp2023\\Backend\\.cursor\\debug.log"),String.format("{\"location\":\"FlightController.java:232\",\"message\":\"After updateVol SUCCESS\",\"data\":{\"flightId\":%d},\"timestamp\":%d,\"sessionId\":\"debug-session\",\"hypothesisId\":\"ALL\"}%n",cancelledFlight.getFlightId(),System.currentTimeMillis()).getBytes(),java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);}catch(Exception e){}
        // #endregion
        
        log.info("Flight {} cancelled successfully", flightId);
        
        // TODO: Notifier les clients avec des réservations sur ce vol
        // TODO: Créer des remboursements automatiques selon les règles
        
        return ResponseEntity.ok(cancelledFlight);
    }

}
