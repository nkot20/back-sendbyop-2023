package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.ReceiverDto;
import com.sendByOP.expedition.models.entities.Receiver;
import com.sendByOP.expedition.services.impl.ReceiverService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/receiver")
@Tag(name = "Receivers", description = "API endpoints for managing receivers")
public class ReceiverController {
    private final ReceiverService receveurService;

    @Operation(summary = "Create a new receiver", description = "Creates a new receiver entry in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Receiver created successfully",
                content = @Content(schema = @Schema(implementation = ReceiverDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(value = "/save")
    public ResponseEntity<?> save(@RequestBody @Parameter(description = "Receiver details") Receiver receiver) {
        ReceiverDto newReceiver = receveurService.save(receiver);
        return new ResponseEntity<>(newReceiver, HttpStatus.CREATED);
    }

}
