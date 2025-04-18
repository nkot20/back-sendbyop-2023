package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.iServices.IAirPortService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.AirportDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/airport")
@RequiredArgsConstructor
@Tag(name = "Airport Management", description = "APIs for managing airport information")
public class AirPortController {
    private final IAirPortService airportService;

    @Operation(summary = "Create a new airport", description = "Creates a new airport entry in the system")
    @ApiResponse(responseCode = "201", description = "Airport created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid airport data provided")
    @PostMapping("/save")
    public ResponseEntity<AirportDto> saveAirport(@RequestBody @Valid AirportDto airportDto) throws SendByOpException {
        AirportDto savedAirport = airportService.saveAeroPort(airportDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAirport);
    }

    @Operation(summary = "Get all airports", description = "Retrieves a list of all airports in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of airports")
    @GetMapping("/")
    public ResponseEntity<List<AirportDto>> getAllAirports() {
        List<AirportDto> airports = airportService.getAllAirport();
        return ResponseEntity.ok(airports);
    }

    @Operation(summary = "Get airport by ID", description = "Retrieves airport information by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the airport")
    @ApiResponse(responseCode = "404", description = "Airport not found")
    @GetMapping("/{id}")
    public ResponseEntity<AirportDto> getAirport(@Parameter(description = "ID of the airport to retrieve") @PathVariable("id") int id) throws SendByOpException {
        AirportDto airport = airportService.getAirport(id);
        return ResponseEntity.ok(airport);
    }
}
