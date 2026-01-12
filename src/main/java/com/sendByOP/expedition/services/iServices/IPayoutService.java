package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PayoutDto;

import java.util.List;

/**
 * Service de gestion des paiements aux voyageurs
 */
public interface IPayoutService {
    
    /**
     * Traite le paiement pour une réservation complétée
     *
     * @param bookingId ID de la réservation
     * @return DTO du payout créé
     * @throws SendByOpException Si erreur lors du traitement
     */
    PayoutDto processPayoutToTraveler(Integer bookingId) throws SendByOpException;
    
    /**
     * Récupère tous les payouts d'un voyageur
     *
     * @param travelerId ID du voyageur
     * @return Liste des payouts
     */
    List<PayoutDto> getPayoutsForTraveler(Integer travelerId);
    
    /**
     * Récupère le payout associé à une réservation
     *
     * @param bookingId ID de la réservation
     * @return DTO du payout ou null si non trouvé
     */
    PayoutDto getPayoutForBooking(Integer bookingId);
    
    /**
     * Traite automatiquement les payouts pour toutes les réservations récupérées
     *
     * @return Nombre de payouts traités
     */
    int processAutomaticPayouts();
}
