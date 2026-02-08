package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les activités récentes du dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
    
    /**
     * Type d'activité: BOOKING, TRIP, PAYMENT, USER, CANCELLATION
     */
    private String type;
    
    /**
     * Action effectuée (ex: "Nouvelle réservation", "Voyage publié")
     */
    private String action;
    
    /**
     * Description détaillée de l'activité
     */
    private String description;
    
    /**
     * Nom de l'utilisateur concerné
     */
    private String userName;
    
    /**
     * Email de l'utilisateur concerné
     */
    private String userEmail;
    
    /**
     * ID de l'entité concernée (booking, flight, etc.)
     */
    private Long entityId;
    
    /**
     * Date et heure de l'activité
     */
    private LocalDateTime timestamp;
}
