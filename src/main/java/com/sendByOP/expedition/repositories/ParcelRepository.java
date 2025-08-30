package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Parcel;
import com.sendByOP.expedition.models.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Integer> {
    public List<Parcel> findByReservation(Booking idRe);
    
    @Query("SELECT SUM(p.weightKg) FROM Parcel p WHERE p.reservation.flight.flightId = :flightId AND p.reservation.paymentStatus = 1")
    Float getTotalWeightByFlightId(@Param("flightId") Integer flightId);
}
