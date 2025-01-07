package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Integer> {

    Optional<Newsletter> findByEmail(String email);
}