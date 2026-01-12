package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PlatformSettingsDto;

/**
 * Service de gestion des paramètres de la plateforme
 * Pattern Singleton: Une seule ligne de paramètres existe
 */
public interface IPlatformSettingsService {
    
    /**
     * Récupère les paramètres de la plateforme
     * Si aucun paramètre n'existe, crée les valeurs par défaut
     * 
     * @return Les paramètres actuels
     * @throws SendByOpException En cas d'erreur
     */
    PlatformSettingsDto getSettings() throws SendByOpException;
    
    /**
     * Met à jour les paramètres de la plateforme
     * Valide toutes les contraintes avant mise à jour:
     * - Somme des pourcentages = 100%
     * - Prix minimum < prix maximum
     * - Délais dans les limites autorisées
     * - Pénalité entre 0 et 1
     * 
     * @param settingsDto Nouveaux paramètres
     * @return Les paramètres mis à jour
     * @throws SendByOpException Si les données sont invalides
     */
    PlatformSettingsDto updateSettings(PlatformSettingsDto settingsDto) throws SendByOpException;
}
