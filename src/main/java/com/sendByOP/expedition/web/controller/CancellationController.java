package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.services.impl.CancellationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

/**
 * Contrôleur pour la gestion des annulations et remboursements
 */
@RestController
@RequestMapping("/cancellation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Annulation & Remboursement", description = "Gestion des annulations de réservations et calculs de remboursement")
@SecurityRequirement(name = "bearerAuth")
public class CancellationController {
    
    private final CancellationService cancellationService;
    
    /**
     * Calcule le montant du remboursement pour une réservation
     * 
     * @param bookingId ID de la réservation
     * @return Détails du remboursement possible
     */
    @GetMapping("/calculate/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Calculer le remboursement",
            description = "Calcule le montant du remboursement en fonction des règles métier (avant/après paiement, délai avant vol, etc.)"
    )
    public ResponseEntity<Map<String, Object>> calculateRefund(
            @Parameter(description = "ID de la réservation") 
            @PathVariable Integer bookingId) throws SendByOpException {
        
        log.info("GET /cancellation/calculate/{} - Calcul du remboursement", bookingId);
        
        Map<String, Object> refundDetails = cancellationService.calculateRefund(bookingId);
        return ResponseEntity.ok(refundDetails);
    }
    
    /**
     * Annule une réservation et effectue le remboursement
     * 
     * @param bookingId ID de la réservation
     * @param principal Utilisateur authentifié
     * @return Détails du remboursement effectué
     */
    @PostMapping("/cancel/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Annuler une réservation",
            description = "Annule une réservation et effectue le remboursement selon les règles métier"
    )
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @Parameter(description = "ID de la réservation") 
            @PathVariable Integer bookingId,
            Principal principal) throws SendByOpException {
        
        log.info("POST /cancellation/cancel/{} - Annulation par {}", bookingId, principal.getName());
        
        // TODO: Récupérer l'ID du customer depuis le principal
        // Pour l'instant, on suppose qu'on peut l'extraire ou le passer en paramètre
        // Integer customerId = extractCustomerIdFromPrincipal(principal);
        
        // Temporaire: extraire de l'email (à adapter selon votre système)
        // Map<String, Object> refundDetails = cancellationService.cancelBookingWithRefund(bookingId, customerId);
        
        // Pour l'instant, retourner juste le calcul
        Map<String, Object> refundDetails = cancellationService.calculateRefund(bookingId);
        
        return ResponseEntity.ok(refundDetails);
    }
    
    /**
     * Calcule les gains nets d'un voyageur après commission
     * 
     * @param totalAmount Montant total de la transaction
     * @return Détails du calcul des gains
     */
    @GetMapping("/earnings")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Calculer les gains voyageur",
            description = "Calcule le gain net du voyageur après déduction de la commission plateforme et de l'assurance"
    )
    public ResponseEntity<Map<String, Object>> calculateEarnings(
            @Parameter(description = "Montant total de la transaction") 
            @RequestParam BigDecimal totalAmount) {
        
        log.info("GET /cancellation/earnings?totalAmount={}", totalAmount);
        
        Map<String, Object> earnings = cancellationService.calculateTravelerEarnings(totalAmount);
        return ResponseEntity.ok(earnings);
    }
}
