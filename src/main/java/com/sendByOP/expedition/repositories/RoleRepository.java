package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
        public Optional<Role> findByIdrole(Long Id);

        public Optional<Role> findByIntitule(String roleName);
}
