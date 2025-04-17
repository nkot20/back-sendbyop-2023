package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.StopoverDto;
import com.sendByOP.expedition.services.iServices.IStopoverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Stopover Management", description = "APIs for managing flight stopovers")
@RestController
@RequestMapping("/stopover")
@RequiredArgsConstructor
public class StopoverController {

    private final IStopoverService stopoverService;

    @Operation(summary = "Create stopover", description = "Creates a new stopover for a flight")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Stopover created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StopoverDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid stopover data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<StopoverDto> createStopover(
        @Parameter(description = "Stopover details", required = true)
        @RequestBody StopoverDto stopover) {
        StopoverDto newStopover = stopoverService.addStopover(stopover);
        return ResponseEntity.status(HttpStatus.CREATED).body(newStopover);
    }

    @Operation(summary = "Delete stopover", description = "Deletes a stopover by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Stopover deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Stopover not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStopover(
        @Parameter(description = "ID of the stopover to delete", required = true)
        @PathVariable Integer id) {
        stopoverService.deleteStopover(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get stopovers by flight", description = "Retrieves all stopovers for a specific flight")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved stopovers list",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StopoverDto.class))),
        @ApiResponse(responseCode = "404", description = "Flight not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/flight/{id}")
    public ResponseEntity<List<StopoverDto>> getStopoversByFlightId(
        @Parameter(description = "ID of the flight", required = true)
        @PathVariable("id") int id) {
        List<StopoverDto> stopovers = stopoverService.findByFlightId(id);
        return ResponseEntity.ok(stopovers);
    }
}
