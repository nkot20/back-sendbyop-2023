package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.entities.BaseEntity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
@EntityListeners(AuditListener.class)
public class AuditListener {

    @PrePersist
    public void beforeInsert(BaseEntity entity) {
        String currentUser = getCurrentUser();
        entity.setCreatedAt(new Date());
        entity.setCreatedBy(currentUser);
    }

    @PreUpdate
    public void beforeUpdate(BaseEntity entity) {
        String currentUser = getCurrentUser();
        entity.setUpdatedAt(new Date());
        entity.setUpdatedBy(currentUser);
    }

    private String getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return "SYSTEM"; // Si l'utilisateur n'est pas identifié
    }
}
