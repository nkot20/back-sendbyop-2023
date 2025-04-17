package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.impl.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Tag(name = "Cache Management", description = "APIs for managing application caches (Admin only)")
public class CacheController {

    private final CacheService cacheService;

    @Operation(summary = "Clear specific cache", description = "Clears a specific cache by name (Admin only)")
    @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - Admin only")
    @DeleteMapping("/{cacheName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> evictCache(@Parameter(description = "Name of the cache to clear") @PathVariable String cacheName) {
        cacheService.evictCache(cacheName);
        return ResponseEntity.ok("Cache '" + cacheName + "' vidé avec succès");
    }

    @Operation(summary = "Clear all caches", description = "Clears all application caches (Admin only)")
    @ApiResponse(responseCode = "200", description = "All caches cleared successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - Admin only")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> evictAllCaches() {
        cacheService.evictAllCaches();
        return ResponseEntity.ok("Tous les caches ont été vidés avec succès");
    }
}