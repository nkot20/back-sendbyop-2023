package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Payout;
import com.sendByOP.expedition.models.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Payout
 */
@Repository
public interface PayoutRepository extends JpaRepository<Payout, Integer> {
    
    /**
     * Recherche les payouts d'un voyageur
     */
    List<Payout> findByTravelerIdOrderByCreatedAtDesc(Integer travelerId);
    
    /**
     * Recherche le payout d'une réservation
     */
    Optional<Payout> findByBookingId(Integer bookingId);
    
    /**
     * Recherche les payouts par statut
     */
    List<Payout> findByStatus(PayoutStatus status);
    
    /**
     * Vérifie si un payout existe pour une réservation
     */
    boolean existsByBookingId(Integer bookingId);
}
