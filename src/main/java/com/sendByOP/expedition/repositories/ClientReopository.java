package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientReopository extends JpaRepository<Customer, Integer> {

    Boolean existsByEmail(String email);

    public Optional<Customer> findByEmail(String email);

    public Optional<Customer> findByTel(String tel);
}
