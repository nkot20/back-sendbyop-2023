package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques utilisateurs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    
    /**
     * Nombre total d'utilisateurs (customers)
     */
    private Long totalUsers;
    
    /**
     * Nombre d'utilisateurs actifs (au moins une réservation)
     */
    private Long activeUsers;
    
    /**
     * Nombre de nouveaux utilisateurs (période donnée)
     */
    private Long newUsers;
    
    /**
     * Nombre de voyageurs (customers avec vols)
     */
    private Long travelers;
    
    /**
     * Nombre d'expéditeurs (customers avec réservations)
     */
    private Long senders;
    
    /**
     * Taux d'utilisateurs actifs (actifs / total)
     */
    private Double activeUserRate;
    
    /**
     * Nombre moyen de réservations par utilisateur actif
     */
    private Double averageBookingsPerUser;
}
