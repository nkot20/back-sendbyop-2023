package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PayoutDto;
import com.sendByOP.expedition.services.iServices.IPayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour les payouts aux voyageurs
 */
@Slf4j
@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
@Tag(name = "Payouts", description = "API pour la gestion des paiements aux voyageurs")
public class PayoutController {

    private final IPayoutService payoutService;

    /**
     * Traiter le payout pour une réservation
     */
    @PostMapping("/{bookingId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Traiter un payout",
            description = "Crée et traite le payout pour une réservation complétée"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payout créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Non autorisé (admin requis)"),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    })
    public ResponseEntity<PayoutDto> processPayout(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId
    ) throws SendByOpException {
        log.info("POST /api/payouts/{}/process", bookingId);
        
        PayoutDto payout = payoutService.processPayoutToTraveler(bookingId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(payout);
    }

    /**
     * Récupérer les payouts d'un voyageur
     */
    @GetMapping("/traveler/{travelerId}")
    @PreAuthorize("hasRole('ADMIN') or #travelerId == principal.id")
    @Operation(
            summary = "Payouts d'un voyageur",
            description = "Récupère tous les payouts d'un voyageur"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payouts récupérés avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    public ResponseEntity<List<PayoutDto>> getPayoutsForTraveler(
            @Parameter(description = "ID du voyageur")
            @PathVariable Integer travelerId
    ) {
        log.info("GET /api/payouts/traveler/{}", travelerId);
        
        List<PayoutDto> payouts = payoutService.getPayoutsForTraveler(travelerId);
        
        return ResponseEntity.ok(payouts);
    }

    /**
     * Récupérer le payout d'une réservation
     */
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Payout d'une réservation",
            description = "Récupère le payout associé à une réservation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payout récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Non autorisé (admin requis)"),
            @ApiResponse(responseCode = "404", description = "Payout non trouvé")
    })
    public ResponseEntity<PayoutDto> getPayoutForBooking(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId
    ) {
        log.info("GET /api/payouts/booking/{}", bookingId);
        
        PayoutDto payout = payoutService.getPayoutForBooking(bookingId);
        
        if (payout == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(payout);
    }
}
