package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BankInfoDto;
import com.sendByOP.expedition.services.iServices.IBankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bank-account-infos")
@Tag(name = "Bank Account Management", description = "APIs for managing bank account information")
public class BankAccountInfoController {

    private final IBankAccountService iInfoBancaireService;

    @Operation(summary = "Save bank account information", description = "Creates or updates bank account information")
    @ApiResponse(responseCode = "201", description = "Bank account information saved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid bank account data")
    @PostMapping("/save")
    public ResponseEntity<?> saveInfo(@RequestBody @Valid BankInfoDto infoBancaire) throws SendByOpException {
        return new ResponseEntity<>(iInfoBancaireService.save(infoBancaire), HttpStatus.CREATED);
    }

    @Operation(summary = "Get bank account information", description = "Retrieves bank account information by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved bank account information")
    @ApiResponse(responseCode = "404", description = "Bank account information not found")
    @GetMapping("/{id}")
    public ResponseEntity<?> getInfoIban(@Parameter(description = "ID of the bank account information") @PathVariable("id") int id) throws SendByOpException {
        return new ResponseEntity<>(iInfoBancaireService.getBankAccountInfos(id), HttpStatus.OK);
    }

    @Operation(summary = "Get bank account information by customer email", 
               description = "Retrieves bank account information for a customer using their email address")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved bank account information")
    @ApiResponse(responseCode = "404", description = "Customer or bank account information not found")
    @ApiResponse(responseCode = "400", description = "Invalid email format")
    @GetMapping("/customer/{email}")
    public ResponseEntity<BankInfoDto> getBankAccountInfosByEmail(
            @Parameter(description = "Customer email address") @PathVariable("email") String email) throws SendByOpException {
        BankInfoDto bankInfo = iInfoBancaireService.getBankAccountInfosByEmail(email);
        return new ResponseEntity<>(bankInfo, HttpStatus.OK);
    }

}
