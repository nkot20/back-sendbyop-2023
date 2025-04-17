package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByUsername(String userId);

    public Boolean existsByEmail(String userName);

    public Optional<User> findByEmail(String email);

}
