package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.impl.FraudGuardService;
import com.sendByOP.expedition.services.impl.FraudGuardService.FraudLimitsDto;
import com.sendByOP.expedition.services.impl.FraudGuardService.UserFraudStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller pour les endpoints anti-fraude
 * Permet aux utilisateurs de voir leurs limites restantes
 */
@Slf4j
@RestController
@RequestMapping("/api/fraud-guard")
@RequiredArgsConstructor
@Tag(name = "Fraud Guard", description = "API de protection anti-fraude")
public class FraudGuardController {

    private final FraudGuardService fraudGuardService;

    @Operation(summary = "Get current fraud limits", description = "Get the current fraud protection limits configured by admin")
    @ApiResponse(responseCode = "200", description = "Limits retrieved successfully")
    @GetMapping("/limits")
    public ResponseEntity<FraudLimitsDto> getCurrentLimits() {
        log.debug("Getting current fraud protection limits");
        FraudLimitsDto limits = fraudGuardService.getCurrentLimits();
        return ResponseEntity.ok(limits);
    }

    @Operation(summary = "Get user fraud status", description = "Get the current user's fraud status including remaining bookings and flights")
    @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    @GetMapping("/status")
    public ResponseEntity<UserFraudStatusDto> getUserStatus(Principal principal) {
        log.debug("Getting fraud status for user: {}", principal.getName());
        UserFraudStatusDto status = fraudGuardService.getUserFraudStatus(principal.getName());
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Get remaining bookings", description = "Get the number of remaining bookings for the current user this week")
    @ApiResponse(responseCode = "200", description = "Remaining bookings retrieved successfully")
    @GetMapping("/remaining-bookings")
    public ResponseEntity<Integer> getRemainingBookings(Principal principal) {
        log.debug("Getting remaining bookings for user: {}", principal.getName());
        int remaining = fraudGuardService.getRemainingBookings(principal.getName());
        return ResponseEntity.ok(remaining);
    }

    @Operation(summary = "Get remaining flights", description = "Get the number of remaining flights for the current user this week")
    @ApiResponse(responseCode = "200", description = "Remaining flights retrieved successfully")
    @GetMapping("/remaining-flights")
    public ResponseEntity<Integer> getRemainingFlights(Principal principal) {
        log.debug("Getting remaining flights for user: {}", principal.getName());
        int remaining = fraudGuardService.getRemainingFlights(principal.getName());
        return ResponseEntity.ok(remaining);
    }
}
