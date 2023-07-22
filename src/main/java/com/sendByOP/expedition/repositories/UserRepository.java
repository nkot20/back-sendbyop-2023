package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByUsername(String iduser);

    public Optional<User> findByUsernameAndPw(String iduser, String pw);

    public Optional<User> findByNom(String userName);

    public Boolean existsByEmail(String userName);

    public Optional<User> findByEmail(String email);

}
