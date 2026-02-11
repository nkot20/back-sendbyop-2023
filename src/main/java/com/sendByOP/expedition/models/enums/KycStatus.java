package com.sendByOP.expedition.models.enums;

/**
 * Statuts de vérification KYC (Know Your Customer)
 */
public enum KycStatus {
    /**
     * Aucun document soumis
     */
    NOT_SUBMITTED("Non soumis", "L'utilisateur n'a pas encore soumis de documents"),
    
    /**
     * Documents soumis, en attente de validation
     */
    PENDING_REVIEW("En attente", "Les documents sont en cours de vérification par l'équipe"),
    
    /**
     * Documents validés
     */
    APPROVED("Validé", "L'identité a été vérifiée et approuvée"),
    
    /**
     * Documents rejetés
     */
    REJECTED("Rejeté", "Les documents ont été rejetés"),
    
    /**
     * Documents expirés
     */
    EXPIRED("Expiré", "Les documents d'identité ont expiré");

    private final String label;
    private final String description;

    KycStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convertit un int (legacy) en KycStatus
     * 0 = NOT_SUBMITTED, 1 = APPROVED
     */
    public static KycStatus fromLegacyInt(int value) {
        return value == 1 ? APPROVED : NOT_SUBMITTED;
    }

    /**
     * Convertit en int pour compatibilité legacy
     */
    public int toLegacyInt() {
        return this == APPROVED ? 1 : 0;
    }
}
