package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.RefundableBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundableBookingRepository extends JpaRepository<RefundableBooking, Integer> {
}
