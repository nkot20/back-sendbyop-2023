package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Review;
import com.sendByOP.expedition.models.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisRepository extends JpaRepository<Review, Integer> {


    public List<Review> findByTransporteurOrderByDateAsc(Customer transporteur);

    public List<Review> findByExpediteurOrderByDateAsc(Customer transporteur);
}
