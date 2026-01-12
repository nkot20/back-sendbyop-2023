package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.BookingStatsDto;
import com.sendByOP.expedition.models.dto.RevenueStatsDto;
import com.sendByOP.expedition.models.dto.UserStatsDto;
import com.sendByOP.expedition.services.iServices.IStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Contrôleur pour les statistiques et analytics
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "API pour les statistiques et analytics")
public class StatisticsController {

    private final IStatisticsService statisticsService;

    /**
     * Récupérer les statistiques de réservations
     */
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Statistiques de réservations",
            description = "Récupère les statistiques détaillées des réservations pour une période donnée"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Non autorisé (admin requis)")
    })
    public ResponseEntity<BookingStatsDto> getBookingStatistics(
            @Parameter(description = "Date de début (format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "Date de fin (format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        log.info("GET /api/statistics/bookings from={} to={}", from, to);
        
        BookingStatsDto stats = statisticsService.getBookingStatistics(from, to);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupérer les statistiques de revenus
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Statistiques de revenus",
            description = "Récupère les statistiques financières pour une période donnée"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Non autorisé (admin requis)")
    })
    public ResponseEntity<RevenueStatsDto> getRevenueStatistics(
            @Parameter(description = "Date de début (format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "Date de fin (format: yyyy-MM-dd)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        log.info("GET /api/statistics/revenue from={} to={}", from, to);
        
        RevenueStatsDto stats = statisticsService.getRevenueStatistics(from, to);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupérer les statistiques utilisateurs
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Statistiques utilisateurs",
            description = "Récupère les statistiques sur les utilisateurs de la plateforme"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Non autorisé (admin requis)")
    })
    public ResponseEntity<UserStatsDto> getUserStatistics() {
        log.info("GET /api/statistics/users");
        
        UserStatsDto stats = statisticsService.getUserStatistics();
        
        return ResponseEntity.ok(stats);
    }
}
