package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Review;
import com.sendByOP.expedition.models.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {


    public List<Review> findByTransporterOrderByDateAsc(Customer customer);

    public List<Review> findByShipperOrderByDateAsc(Customer customer);
}
