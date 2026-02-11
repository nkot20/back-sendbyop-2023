package com.sendByOP.expedition.web.controller.admin;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PlatformSettingsDto;
import com.sendByOP.expedition.services.iServices.IPlatformSettingsService;
import com.sendByOP.expedition.services.impl.FraudGuardService;
import com.sendByOP.expedition.services.impl.FraudGuardService.FraudLimitsDto;
import com.sendByOP.expedition.services.impl.FraudGuardService.UserFraudStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des paramètres de la plateforme
 * Accessible uniquement aux administrateurs
 */
@Slf4j
@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@Tag(name = "Platform Settings (Admin)", description = "Gestion des paramètres de la plateforme")
@PreAuthorize("hasRole('ADMIN')")
public class PlatformSettingsController {

    private final IPlatformSettingsService platformSettingsService;
    private final FraudGuardService fraudGuardService;

    /**
     * Récupère les paramètres de la plateforme
     */
    @GetMapping
    @Operation(summary = "Récupérer les paramètres de la plateforme",
            description = "Récupère les paramètres de configuration actifs de la plateforme")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paramètres récupérés avec succès",
                    content = @Content(schema = @Schema(implementation = PlatformSettingsDto.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Administrateur requis",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur serveur",
                    content = @Content)
    })
    public ResponseEntity<PlatformSettingsDto> getSettings() throws SendByOpException {
        log.info("GET /admin/settings - Récupération des paramètres");
        
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        log.info("Settings retrieved successfully");
        return ResponseEntity.ok(settings);
    }

    /**
     * Met à jour les paramètres de la plateforme
     */
    @PutMapping
    @Operation(summary = "Mettre à jour les paramètres de la plateforme",
            description = "Met à jour les paramètres de configuration de la plateforme. " +
                    "Valide que la somme des pourcentages = 100%, que le prix min < prix max, " +
                    "et que tous les délais sont dans les limites autorisées.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paramètres mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = PlatformSettingsDto.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Administrateur requis",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur serveur",
                    content = @Content)
    })
    public ResponseEntity<PlatformSettingsDto> updateSettings(
            @Valid @RequestBody PlatformSettingsDto settingsDto) throws SendByOpException {
        
        log.info("PUT /admin/settings - Mise à jour des paramètres");
        log.debug("New settings: {}", settingsDto);
        
        PlatformSettingsDto updated = platformSettingsService.updateSettings(settingsDto);
        
        log.info("Settings updated successfully");
        return ResponseEntity.ok(updated);
    }

    /**
     * Endpoint pour réinitialiser les paramètres aux valeurs par défaut
     */
    @PostMapping("/reset")
    @Operation(summary = "Réinitialiser les paramètres par défaut",
            description = "Réinitialise tous les paramètres à leurs valeurs par défaut")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paramètres réinitialisés avec succès",
                    content = @Content(schema = @Schema(implementation = PlatformSettingsDto.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Administrateur requis",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur serveur",
                    content = @Content)
    })
    public ResponseEntity<PlatformSettingsDto> resetToDefaults() throws SendByOpException {
        log.info("POST /admin/settings/reset - Réinitialisation des paramètres");
        
        // Créer un DTO avec les valeurs par défaut
        PlatformSettingsDto defaultSettings = PlatformSettingsDto.builder()
                .minPricePerKg(java.math.BigDecimal.valueOf(5.00))
                .maxPricePerKg(java.math.BigDecimal.valueOf(50.00))
                .travelerPercentage(java.math.BigDecimal.valueOf(70.00))
                .platformPercentage(java.math.BigDecimal.valueOf(25.00))
                .vatPercentage(java.math.BigDecimal.valueOf(5.00))
                .paymentTimeoutHours(12)
                .autoPayoutDelayHours(24)
                .cancellationDeadlineHours(24)
                .lateCancellationPenalty(java.math.BigDecimal.valueOf(0.50))
                .maxBookingsPerWeek(2)
                .maxFlightsPerWeek(2)
                .fraudProtectionEnabled(true)
                .build();
        
        PlatformSettingsDto reset = platformSettingsService.updateSettings(defaultSettings);
        
        log.info("Settings reset to defaults successfully");
        return ResponseEntity.ok(reset);
    }

    // ==========================================
    // ENDPOINTS ANTI-FRAUDE
    // ==========================================

    /**
     * Récupère les limites anti-fraude actuelles
     */
    @GetMapping("/fraud-limits")
    @Operation(summary = "Récupérer les limites anti-fraude",
            description = "Récupère les limites anti-fraude actuellement configurées")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Limites récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = FraudLimitsDto.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé",
                    content = @Content)
    })
    public ResponseEntity<FraudLimitsDto> getFraudLimits() {
        log.info("GET /admin/settings/fraud-limits - Récupération des limites anti-fraude");
        FraudLimitsDto limits = fraudGuardService.getCurrentLimits();
        return ResponseEntity.ok(limits);
    }

    /**
     * Récupère le statut anti-fraude d'un utilisateur spécifique
     */
    @GetMapping("/fraud-status/{email}")
    @Operation(summary = "Récupérer le statut anti-fraude d'un utilisateur",
            description = "Récupère le statut anti-fraude d'un utilisateur spécifique, incluant le nombre de réservations et voyages cette semaine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = UserFraudStatusDto.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé",
                    content = @Content)
    })
    public ResponseEntity<UserFraudStatusDto> getUserFraudStatus(
            @Parameter(description = "Email de l'utilisateur") @PathVariable String email) {
        log.info("GET /admin/settings/fraud-status/{} - Récupération du statut anti-fraude", email);
        UserFraudStatusDto status = fraudGuardService.getUserFraudStatus(email);
        return ResponseEntity.ok(status);
    }
}
