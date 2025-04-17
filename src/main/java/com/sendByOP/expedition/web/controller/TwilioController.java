package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PhoneVerificationRequestDto;
import com.sendByOP.expedition.models.dto.VerificationResult;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.impl.PhoneVerificationService;
import com.sendByOP.expedition.services.impl.UserRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Phone Verification", description = "APIs for phone number verification using Twilio")
@RestController
@RequestMapping("/api/v1/phone-verification")
@RequiredArgsConstructor
public class TwilioController {

    private final PhoneVerificationService phoneVerificationService;
    private final UserRegistrationService userRegistrationService;

    @Operation(summary = "Send verification code", description = "Sends a verification code to the specified phone number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification code sent successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
        @ApiResponse(responseCode = "400", description = "Invalid phone number format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationCode(
        @Parameter(description = "Phone number verification request", required = true)
        @RequestBody PhoneVerificationRequestDto request) throws SendByOpException {
        VerificationResult result = phoneVerificationService.startVerification(request.getPhoneNumber());
        return ResponseEntity.ok(new ResponseMessage("Verification code sent successfully"));
    }

    @Operation(summary = "Verify phone number", description = "Verifies the phone number using the provided verification code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phone number verified successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
        @ApiResponse(responseCode = "400", description = "Invalid verification code"),
        @ApiResponse(responseCode = "404", description = "Phone number not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/verify/{phoneNumber}")
    public ResponseEntity<?> verifyPhoneNumber(
            @Parameter(description = "Phone number to verify", required = true)
            @PathVariable String phoneNumber,
            @Parameter(description = "Verification code received via SMS", required = true)
            @RequestBody String verificationCode) throws SendByOpException {
        VerificationResult verificationResult = phoneVerificationService.checkVerification(phoneNumber, verificationCode);
        if (verificationResult.isValid()) {
            userRegistrationService.registerUserAfterPhoneVerification(phoneNumber);
            return ResponseEntity.ok(new ResponseMessage("Phone number verified and user registered successfully"));
        }
        return ResponseEntity.badRequest()
                .body(new ResponseMessage("Invalid verification code"));
    }


}
