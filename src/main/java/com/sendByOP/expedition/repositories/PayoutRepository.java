package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Payout;
import com.sendByOP.expedition.models.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Méthodes pour l'administration
    Page<Payout> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Payout> findByStatusOrderByCreatedAtDesc(PayoutStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Payout p WHERE " +
           "LOWER(p.traveler.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.traveler.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.traveler.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(p.booking.id AS string) LIKE CONCAT('%', :search, '%') " +
           "ORDER BY p.createdAt DESC")
    Page<Payout> searchPayouts(@Param("search") String search, Pageable pageable);
}
