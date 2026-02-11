package com.sendByOP.expedition.models.enums;

/**
 * Statuts possibles pour un vol
 */
public enum FlightStatus {
    /**
     * Vol en attente de validation par l'administrateur
     * Le voyageur a publié son vol mais il doit être validé avant d'être visible
     */
    PENDING_VALIDATION("En attente de validation"),
    
    /**
     * Vol actif - validé par l'admin et peut recevoir des réservations
     */
    ACTIVE("Actif"),
    
    /**
     * Vol rejeté par l'administrateur
     */
    REJECTED("Rejeté"),
    
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
