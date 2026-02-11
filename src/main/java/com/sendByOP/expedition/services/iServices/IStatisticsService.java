package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.BookingStatsDto;
import com.sendByOP.expedition.models.dto.RecentActivityDto;
import com.sendByOP.expedition.models.dto.RevenueStatsDto;
import com.sendByOP.expedition.models.dto.UserStatsDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service de statistiques et analytics pour SendByOp
 */
public interface IStatisticsService {
    
    /**
     * Récupère les statistiques de réservations pour une période donnée
     *
     * @param from Date de début (null = depuis le début)
     * @param to Date de fin (null = jusqu'à maintenant)
     * @return Statistiques de réservations
     */
    BookingStatsDto getBookingStatistics(LocalDate from, LocalDate to);
    
    /**
     * Récupère les statistiques de revenus pour une période donnée
     *
     * @param from Date de début (null = depuis le début)
     * @param to Date de fin (null = jusqu'à maintenant)
     * @return Statistiques de revenus
     */
    RevenueStatsDto getRevenueStatistics(LocalDate from, LocalDate to);
    
    /**
     * Récupère les statistiques utilisateurs
     *
     * @return Statistiques utilisateurs
     */
    UserStatsDto getUserStatistics();
    
    /**
     * Récupère les activités récentes de la plateforme
     *
     * @param limit Nombre maximum d'activités à retourner
     * @return Liste des activités récentes
     */
    List<RecentActivityDto> getRecentActivity(int limit);
}