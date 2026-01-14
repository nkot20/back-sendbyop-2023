package com.sendByOP.expedition.models.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    PENDING("En attente", "La transaction est en cours de traitement"),
    PROCESSING("En cours", "Le paiement est en cours de vérification"),
    COMPLETED("Complétée", "Le paiement a été effectué avec succès"),
    FAILED("Échouée", "Le paiement a échoué"),
    CANCELLED("Annulée", "Le paiement a été annulé"),
    REFUNDED("Remboursée", "Le paiement a été remboursé");
    
    private final String displayName;
    private final String description;
    
    TransactionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
