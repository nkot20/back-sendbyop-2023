package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PaymentDto;
import com.sendByOP.expedition.models.dto.PaymentHistoryDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.services.iServices.IPaymentService;
import com.sendByOP.expedition.reponse.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@Tag(name = "Paiements", description = "API de gestion des paiements")
public class PaymentController {

    private final IPaymentService paiementService;

    @GetMapping("/")
    @Operation(summary = "Récupérer tous les paiements", description = "Admin uniquement")
    public ResponseEntity<?> getAllPayments() {
        try {
            List<PaymentDto> payments = paiementService.getAll();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{email}")
    @Operation(summary = "Récupérer les paiements d'un client par email")
    public ResponseEntity<?> getPaymentsByClient(@PathVariable("email") String email) {
        try {
            List<PaymentDto> payments = paiementService.getPaymentsByClient(email);
            return ResponseEntity.ok(payments);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @PostMapping("/reservation/{id}")
    @Operation(summary = "Traiter un paiement pour une réservation")
    public ResponseEntity<?> processPayment(@PathVariable("id") int id, @RequestBody PaymentDto payment) throws SendByOpException {
        BookingDto reservation = paiementService.processPayment(id, payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    // ==================== NOUVEAUX ENDPOINTS POUR L'HISTORIQUE ====================

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Récupérer l'historique des paiements de l'utilisateur connecté",
            description = "Retourne l'historique des paiements avec pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client non trouvé", content = @Content)
    })
    public ResponseEntity<Page<PaymentHistoryDto>> getMyPaymentHistory(
            @Parameter(description = "Numéro de page (0-indexé)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) throws SendByOpException {
        String email = authentication.getName();
        log.info("GET /payment/history - Fetching payment history for: {}", email);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentHistoryDto> historyPage = paiementService.getPaymentHistory(email, pageable);
        
        return ResponseEntity.ok(historyPage);
    }

    @GetMapping("/history/all")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Récupérer tout l'historique des paiements de l'utilisateur connecté",
            description = "Retourne la liste complète de l'historique des paiements (sans pagination)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client non trouvé", content = @Content)
    })
    public ResponseEntity<List<PaymentHistoryDto>> getMyPaymentHistoryAll(
            Authentication authentication
    ) throws SendByOpException {
        String email = authentication.getName();
        log.info("GET /payment/history/all - Fetching all payment history for: {}", email);
        
        List<PaymentHistoryDto> history = paiementService.getPaymentHistoryAll(email);
        
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/stats")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Récupérer les statistiques de paiement de l'utilisateur connecté",
            description = "Retourne le nombre total de paiements, le montant total et la moyenne"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client non trouvé", content = @Content)
    })
    public ResponseEntity<IPaymentService.PaymentStatsDto> getMyPaymentStats(
            Authentication authentication
    ) throws SendByOpException {
        String email = authentication.getName();
        log.info("GET /payment/history/stats - Fetching payment stats for: {}", email);
        
        IPaymentService.PaymentStatsDto stats = paiementService.getPaymentStats(email);
        
        return ResponseEntity.ok(stats);
    }
}
