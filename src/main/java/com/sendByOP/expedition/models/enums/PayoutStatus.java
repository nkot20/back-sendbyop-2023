package com.sendByOP.expedition.models.enums;

/**
 * Statut d'un versement (payout) au voyageur
 * 
 * @author SendByOp Team
 * @since 2.0.0
 */
public enum PayoutStatus {
    /**
     * Versement en attente de traitement
     */
    PENDING("En attente"),
    
    /**
     * Versement en cours de traitement
     */
    PROCESSING("En cours"),
    
    /**
     * Versement complété avec succès
     */
    COMPLETED("Complété"),
    
    /**
     * Versement échoué
     */
    FAILED("Échoué"),
    
    /**
     * Versement annulé
     */
    CANCELLED("Annulé");
    
    private final String displayName;
    
    PayoutStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Vérifie si le versement est finalisé (succès ou échec)
     */
    public boolean isFinalized() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
