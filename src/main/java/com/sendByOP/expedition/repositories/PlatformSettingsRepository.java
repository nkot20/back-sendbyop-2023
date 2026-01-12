package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.PlatformSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour la gestion des paramètres de la plateforme
 * Note: Une seule ligne de paramètres existe dans la base (singleton)
 */
@Repository
public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Integer> {
    
    /**
     * Récupère les paramètres actifs de la plateforme
     * Retourne la première (et unique) ligne de configuration
     */
    default PlatformSettings getSettings() {
        return findAll().stream()
            .findFirst()
            .orElse(null);
    }
}
