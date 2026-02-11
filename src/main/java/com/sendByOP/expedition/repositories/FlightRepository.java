package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {

    public List<Flight> findAllByOrderByDepartureDateDesc();

    public List<Flight> findByCustomerOrderByPublicationDateDesc(Customer customerId);

    public List<Flight> findByValidationStatus(int status);

    public Optional<Flight> findByFlightId(int id);

    public List<Flight> findByValidationStatusAndCancelledOrderByDepartureDateDesc(int validationStatus, int cancelled);

    @Query("SELECT f FROM Flight f WHERE f.validationStatus = :validationStatus AND f.cancelled = :cancelled AND f.departureDate > CURRENT_TIMESTAMP ORDER BY f.departureDate DESC")
    public Page<Flight> findByValidationStatusAndCancelledAndDepartureDateAfterOrderByDepartureDateDesc(@Param("validationStatus") int validationStatus, @Param("cancelled") int cancelled, Pageable pageable);

    /**
     * Compte les vols publiés par un client dans une période donnée (pour anti-fraude)
     * Exclut les vols annulés
     */
    @Query("SELECT COUNT(f) FROM Flight f WHERE f.customer.id = :customerId " +
           "AND f.publicationDate >= :startDate " +
           "AND f.cancelled = 0")
    long countFlightsByCustomerInPeriod(@Param("customerId") Integer customerId, @Param("startDate") java.util.Date startDate);

    /**
     * Compte les vols publiés par email dans une période donnée
     */
    @Query("SELECT COUNT(f) FROM Flight f WHERE f.customer.email = :email " +
           "AND f.publicationDate >= :startDate " +
           "AND f.cancelled = 0")
    long countFlightsByEmailInPeriod(@Param("email") String email, @Param("startDate") java.util.Date startDate);

    /**
     * Récupère les vols par statut triés par date de publication décroissante
     */
    List<Flight> findByStatusOrderByPublicationDateDesc(com.sendByOP.expedition.models.enums.FlightStatus status);

}

