package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.mappers.VisitorMapper;
import com.sendByOP.expedition.models.dto.VisitorDto;
import com.sendByOP.expedition.services.iServices.IVisiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/visitors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Visitor", description = "Visitor management APIs")
public class VisitorController {

    private final IVisiteService visiteService;
    private final VisitorMapper visitorMapper;

    @Operation(summary = "Create a new visitor", description = "Creates a new visitor in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Visitor created successfully",
                content = @Content(schema = @Schema(implementation = VisitorDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<?> addVisitor(@Valid @RequestBody VisitorDto visitorDto) {
        log.debug("Adding new visitor");
        visitorDto.setId(null);
        var savedVisitor = visiteService.addVisitor(visitorMapper.toEntity(visitorDto));
        return new ResponseEntity<>(visitorMapper.toDto(savedVisitor), HttpStatus.CREATED);
    }

    @Operation(summary = "Get total visitor count", description = "Returns the total number of visitors")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved visitor count")
    @GetMapping("/count")
    public ResponseEntity<Integer> getVisitorCount() {
        log.debug("Getting visitor count");
        return new ResponseEntity<>(visiteService.getVisitorCount(), HttpStatus.OK);
    }

}
