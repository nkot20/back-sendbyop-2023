package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO pour les statistiques de réservations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatsDto {
    
    /**
     * Nombre total de réservations
     */
    private Long totalBookings;
    
    /**
     * Nombre de réservations par statut
     * Clé: nom du statut (PENDING_CONFIRMATION, CONFIRMED_PAID, etc.)
     * Valeur: nombre de réservations
     */
    private Map<String, Long> bookingsByStatus;
    
    /**
     * Nombre de réservations en attente de confirmation
     */
    private Long pendingConfirmation;
    
    /**
     * Nombre de réservations confirmées non payées
     */
    private Long confirmedUnpaid;
    
    /**
     * Nombre de réservations confirmées payées
     */
    private Long confirmedPaid;
    
    /**
     * Nombre de réservations livrées
     */
    private Long delivered;
    
    /**
     * Nombre de réservations récupérées (complétées)
     */
    private Long pickedUp;
    
    /**
     * Nombre de réservations annulées (tous types)
     */
    private Long cancelled;
    
    /**
     * Nombre de réservations annulées par le client
     */
    private Long cancelledByClient;
    
    /**
     * Nombre de réservations annulées par le voyageur
     */
    private Long cancelledByTraveler;
    
    /**
     * Nombre de réservations annulées par timeout paiement
     */
    private Long cancelledPaymentTimeout;
    
    /**
     * Taux de conversion (réservations complétées / total)
     */
    private Double conversionRate;
    
    /**
     * Taux d'annulation (annulées / total)
     */
    private Double cancellationRate;
}
