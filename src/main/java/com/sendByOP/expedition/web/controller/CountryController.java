package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.CountryDto;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.iServices.ICountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/country")
@Tag(name = "Country Management", description = "APIs for managing country information")
public class CountryController {

    private final ICountryService paysService;

    @Operation(summary = "Create a new country", description = "Creates a new country entry in the system")
    @ApiResponse(responseCode = "201", description = "Country created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid country data")
    @PostMapping(value = "/save")
    public ResponseEntity<?> saveCountry(@RequestBody @Valid CountryDto pays) {
        CountryDto country = paysService.saveCountry(pays);

        if(country == null) {
            return new ResponseEntity<>(new ResponseMessage("Unn problème est survenu"), HttpStatus.OK);
        }

        return new ResponseEntity<>(country, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all countries", description = "Retrieves a list of all countries in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of countries")
    @GetMapping(value = "/")
    public ResponseEntity<?> getAllCountry() {
        return new ResponseEntity<>(paysService.getCountry(), HttpStatus.OK);
    }
}
