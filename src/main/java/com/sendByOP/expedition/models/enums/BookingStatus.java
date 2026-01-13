package com.sendByOP.expedition.models.enums;

/**
 * Statut d'une réservation dans le processus de réservation
 * 
 * @author SendByOp Team
 * @since 2.0.0
 */
public enum BookingStatus {
    /**
     * En attente de confirmation par le voyageur
     * État initial lors de la création de la réservation
     */
    PENDING_CONFIRMATION("En attente de confirmation"),
    
    /**
     * Confirmée par le voyageur mais non payée
     * Le client a un délai (par défaut 12h) pour effectuer le paiement
     */
    CONFIRMED_UNPAID("Confirmée (non payée)"),
    
    /**
     * Confirmée et payée par le client
     * La réservation est active et en attente du départ du vol
     */
    CONFIRMED_PAID("Confirmée et payée"),
    
    /**
     * Colis remis au voyageur par le client
     * Le client confirme avoir donné le colis au voyageur
     */
    PARCEL_HANDED_TO_TRAVELER("Colis remis au voyageur"),
    
    /**
     * Voyageur confirme avoir reçu le colis
     * Le colis est en possession du voyageur
     */
    PARCEL_RECEIVED_BY_TRAVELER("Colis reçu par le voyageur"),
    
    /**
     * Colis en transit
     * Le vol a décollé avec le colis
     */
    IN_TRANSIT("En transit"),
    
    /**
     * Voyageur confirme avoir remis le colis au destinataire
     * En attente de confirmation du destinataire
     */
    PARCEL_DELIVERED_TO_RECEIVER("Colis remis au destinataire"),
    
    /**
     * Colis livré au destinataire
     * En attente de confirmation de réception
     */
    DELIVERED("Livré"),
    
    /**
     * Réception confirmée par le destinataire
     * Le versement au voyageur est déclenché
     */
    CONFIRMED_BY_RECEIVER("Réception confirmée"),
    
    /**
     * Colis récupéré (alias de CONFIRMED_BY_RECEIVER)
     * Pour compatibilité avec nouveau code
     */
    PICKED_UP("Récupéré"),
    
    /**
     * Annulée par le client
     * Remboursement calculé selon le délai d'annulation
     */
    CANCELLED_BY_CLIENT("Annulée par le client"),
    
    /**
     * Rejetée par le voyageur
     * Le voyageur a refusé de prendre le colis
     */
    CANCELLED_BY_TRAVELER("Rejetée par le voyageur"),
    
    /**
     * Annulée automatiquement pour dépassement du délai de paiement
     * Le client n'a pas payé dans les 12h après confirmation
     */
    CANCELLED_PAYMENT_TIMEOUT("Annulée (délai de paiement dépassé)"),
    
    /**
     * Remboursée
     * Le remboursement a été traité suite à une annulation
     */
    REFUNDED("Remboursée");
    
    private final String displayName;
    
    BookingStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Vérifie si le statut est un statut d'annulation
     */
    public boolean isCancelled() {
        return this == CANCELLED_BY_CLIENT 
            || this == CANCELLED_BY_TRAVELER 
            || this == CANCELLED_PAYMENT_TIMEOUT;
    }
    
    /**
     * Vérifie si le statut permet l'annulation par le client
     */
    public boolean canBeCancelledByClient() {
        return this == PENDING_CONFIRMATION 
            || this == CONFIRMED_UNPAID 
            || this == CONFIRMED_PAID;
    }
    
    /**
     * Vérifie si le statut nécessite un paiement
     */
    public boolean requiresPayment() {
        return this == CONFIRMED_UNPAID;
    }
    
    /**
     * Vérifie si le statut est actif (réservation en cours)
     */
    public boolean isActive() {
        return !isCancelled() && this != REFUNDED 
            && this != CONFIRMED_BY_RECEIVER;
    }
}
