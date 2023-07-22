package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientReopository extends JpaRepository<Client, Integer> {

    Boolean existsByEmail(String email);

    public Optional<Client> findByEmail(String email);

    public Optional<Client> findByTel(String tel);
}
