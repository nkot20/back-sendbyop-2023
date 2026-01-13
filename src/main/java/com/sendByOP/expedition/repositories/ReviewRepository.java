package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Review;
import com.sendByOP.expedition.models.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByTransporterOrderByDateAsc(Customer customer);

    List<Review> findByShipperOrderByDateAsc(Customer customer);
    
    // Nouvelles méthodes pour les avis de réservation
    Optional<Review> findByBookingId(Integer bookingId);
    
    List<Review> findByTransporterIdAndBookingIsNotNull(Integer transporterId);
    
    List<Review> findByShipperIdAndBookingIsNotNull(Integer shipperId);
}
