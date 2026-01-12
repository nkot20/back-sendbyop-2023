package com.sendByOP.expedition.models.enums;

/**
 * Statut d'un destinataire (receiver) dans le système
 * 
 * @author SendByOp Team
 * @since 2.0.0
 */
public enum RecipientStatus {
    /**
     * Destinataire actif
     * Peut recevoir des colis
     */
    ACTIVE("Actif"),
    
    /**
     * Destinataire inactif
     * Temporairement désactivé
     */
    INACTIVE("Inactif"),
    
    /**
     * Destinataire bloqué
     * Ne peut plus recevoir de colis (fraude, abus, etc.)
     */
    BLOCKED("Bloqué");
    
    private final String displayName;
    
    RecipientStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Vérifie si le destinataire peut recevoir des colis
     */
    public boolean canReceiveParcels() {
        return this == ACTIVE;
    }
}
