package com.sendByOP.expedition.models.enums;

/**
 * Types de notifications envoyées dans le processus de réservation
 * 
 * @author SendByOp Team
 * @since 2.0.0
 */
public enum NotificationType {
    /**
     * Notification envoyée lors de la création d'une réservation
     * Destinataires: Voyageur, Client, Destinataire
     */
    BOOKING_CREATED("Nouvelle réservation créée"),
    
    /**
     * Notification envoyée quand le voyageur confirme la réservation
     * Destinataires: Client
     */
    BOOKING_CONFIRMED("Réservation confirmée par le voyageur"),
    
    /**
     * Notification envoyée quand le voyageur rejette la réservation
     * Destinataires: Client
     */
    BOOKING_REJECTED("Réservation rejetée par le voyageur"),
    
    /**
     * Notification envoyée après réception du paiement
     * Destinataires: Client, Voyageur, Destinataire
     */
    PAYMENT_RECEIVED("Paiement reçu et confirmé"),
    
    /**
     * Rappel de paiement avant expiration du délai
     * Destinataires: Client
     */
    PAYMENT_REMINDER("Rappel de paiement"),
    
    /**
     * Notification envoyée quand le destinataire confirme la livraison
     * Destinataires: Client, Voyageur
     */
    DELIVERY_CONFIRMED("Livraison confirmée par le destinataire"),
    
    /**
     * Notification envoyée lors de l'annulation d'une réservation
     * Destinataires: Voyageur, Client, Destinataire (selon qui annule)
     */
    BOOKING_CANCELLED("Réservation annulée"),
    
    /**
     * Notification envoyée après traitement d'un remboursement
     * Destinataires: Client
     */
    REFUND_PROCESSED("Remboursement effectué");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Retourne le template d'email à utiliser pour ce type de notification
     */
    public String getEmailTemplate() {
        return "emails/" + this.name().toLowerCase().replace("_", "-");
    }
}
