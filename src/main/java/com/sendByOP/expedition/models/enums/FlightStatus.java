package com.sendByOP.expedition.models.enums;

/**
 * Statuts possibles pour un vol
 */
public enum FlightStatus {
    /**
     * Vol actif - peut recevoir des réservations
     */
    ACTIVE("Actif"),
    
    /**
     * Vol expiré - la date/heure d'arrivée est passée
     * Aucune nouvelle réservation ne peut être créée
     */
    EXPIRED("Expiré"),
    
    /**
     * Vol annulé par le voyageur
     */
    CANCELLED("Annulé");
    
    private final String displayName;
    
    FlightStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
