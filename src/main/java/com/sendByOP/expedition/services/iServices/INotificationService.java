package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.entities.Booking;

/**
 * Service de notification pour l'envoi d'emails aux clients et voyageurs
 */
public interface INotificationService {
    
    /**
     * Envoie une notification de confirmation de réservation au client
     *
     * @param booking Réservation créée
     */
    void sendBookingConfirmation(Booking booking);
    
    /**
     * Envoie une notification au voyageur qu'une nouvelle réservation l'attend
     *
     * @param booking Réservation en attente
     */
    void sendBookingPendingToTraveler(Booking booking);
    
    /**
     * Envoie un rappel de paiement au client
     *
     * @param booking Réservation confirmée non payée
     * @param hoursRemaining Heures restantes avant deadline
     */
    void sendPaymentReminder(Booking booking, int hoursRemaining);
    
    /**
     * Envoie une notification de livraison au client
     *
     * @param booking Réservation livrée
     */
    void sendDeliveryNotification(Booking booking);
    
    /**
     * Envoie une notification d'annulation
     *
     * @param booking Réservation annulée
     * @param reason Raison de l'annulation
     */
    void sendCancellationNotice(Booking booking, String reason);
    
    /**
     * Envoie une confirmation de récupération au client et voyageur
     *
     * @param booking Réservation récupérée
     */
    void sendPickupConfirmation(Booking booking);
}
