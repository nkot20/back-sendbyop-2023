package com.sendByOP.expedition.models.enums;

/**
 * Statut du compte utilisateur
 */
public enum AccountStatus {
    /**
     * Compte en attente de vérification d'email
     */
    PENDING_VERIFICATION,
    
    /**
     * Compte actif (email vérifié)
     */
    ACTIVE,
    
    /**
     * Compte bloqué (violation de règles, fraude, etc.)
     */
    BLOCKED,
    
    /**
     * Compte inactif (désactivé par l'utilisateur ou l'admin)
     */
    INACTIVE
}
