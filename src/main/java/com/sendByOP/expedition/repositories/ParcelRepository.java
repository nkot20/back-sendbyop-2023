package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Parcel;
import com.sendByOP.expedition.models.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Integer> {
    public List<Parcel> findByReservation(Booking idRe);
}
