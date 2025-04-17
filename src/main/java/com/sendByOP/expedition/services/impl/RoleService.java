package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.Role;
import com.sendByOP.expedition.repositories.RoleRepository;
import com.sendByOP.expedition.services.iServices.IRoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;

    public Role findByRoleName(String roleName) {
        log.debug("Finding role by name: {}", roleName);
        return roleRepository.findByLabel(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }

    public Role getRoleInfo(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role parameter cannot be null or empty");
        }

        log.debug("Getting role info for: {}", role);
        
        String normalizedRole = role.toLowerCase().trim();
        if ("admin".equals(normalizedRole) || "superadmin".equals(normalizedRole)) {
            Optional<Role> roleOptional = roleRepository.findByLabel(normalizedRole);
            return roleOptional.orElseThrow(() -> 
                new IllegalStateException("Required role not found in database: " + normalizedRole));
        }

        throw new IllegalArgumentException("Invalid role type: " + role);
    }

}
