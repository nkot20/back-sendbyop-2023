package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByCustomerIdOrderByBookingDateDesc(Integer customerId);

    List<Booking> findByExpeditionStatus(int value);

    List<Booking> findByBookingDate(Date bookingDate);

    Optional<Booking> findById(Integer id);

    List<Booking> findAllByOrderByBookingDateDesc();

    List<Booking> findByFlightFlightIdAndPaymentStatus(Integer flightId, Integer paymentStatus);

    List<Booking> findByCustomerEmailOrderByBookingDateDesc(String email);

    Page<Booking> findByCustomerEmailOrderByBookingDateDesc(String email, Pageable pageable);

    /**
     * Recherche les réservations non payées dont la deadline est dépassée
     * Optimisé pour l'annulation automatique
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.paymentDeadline < :now")
    List<Booking> findUnpaidWithExpiredDeadline(
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );

    /**
     * Recherche les réservations récupérées qui n'ont pas encore été payées au voyageur
     * Utilisé pour le payout automatique
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") BookingStatus status);

    /**
     * Compte les réservations par statut
     */
    long countByStatus(BookingStatus status);
    
    /**
     * Recherche les réservations faites sur les vols d'un voyageur (par email du voyageur)
     * Utile pour que le voyageur puisse voir les demandes de réservation sur ses vols
     */
    @Query("SELECT b FROM Booking b WHERE b.flight.customer.email = :travelerEmail ORDER BY b.bookingDate DESC")
    List<Booking> findByFlightCustomerEmailOrderByBookingDateDesc(@Param("travelerEmail") String travelerEmail);
    
    /**
     * Version paginée de la recherche des réservations sur les vols d'un voyageur
     */
    @Query("SELECT b FROM Booking b WHERE b.flight.customer.email = :travelerEmail ORDER BY b.bookingDate DESC")
    Page<Booking> findByFlightCustomerEmailOrderByBookingDateDesc(@Param("travelerEmail") String travelerEmail, Pageable pageable);
    
    /**
     * Recherche les réservations en attente de confirmation sur les vols d'un voyageur
     */
    @Query("SELECT b FROM Booking b WHERE b.flight.customer.email = :travelerEmail AND b.status = :status ORDER BY b.bookingDate DESC")
    List<Booking> findByFlightCustomerEmailAndStatusOrderByBookingDateDesc(
            @Param("travelerEmail") String travelerEmail, 
            @Param("status") BookingStatus status
    );

    /**
     * Récupère toutes les réservations avec pagination (pour admin)
     */
    Page<Booking> findAllByOrderByBookingDateDesc(Pageable pageable);

    /**
     * Récupère toutes les réservations par statut avec pagination (pour admin)
     */
    Page<Booking> findByStatusOrderByBookingDateDesc(BookingStatus status, Pageable pageable);

    /**
     * Recherche les réservations par nom client ou destinataire (pour admin)
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "LOWER(b.customer.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.customer.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.customer.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.receiver.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.receiver.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(b.id AS string) LIKE CONCAT('%', :search, '%') " +
           "ORDER BY b.bookingDate DESC")
    Page<Booking> searchBookings(@Param("search") String search, Pageable pageable);
}
