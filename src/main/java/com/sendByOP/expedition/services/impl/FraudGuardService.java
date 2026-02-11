package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.PlatformSettings;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.repositories.FlightRepository;
import com.sendByOP.expedition.repositories.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

/**
 * Service de protection anti-fraude
 * Gère les limites de réservations et de vols par utilisateur
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudGuardService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final PlatformSettingsRepository platformSettingsRepository;

    /**
     * Vérifie si un utilisateur peut faire une nouvelle réservation
     * @param customerId ID du client
     * @param email Email du client
     * @throws SendByOpException si la limite est atteinte
     */
    public void validateBookingLimit(Integer customerId, String email) throws SendByOpException {
        PlatformSettings settings = getSettings();
        
        if (!Boolean.TRUE.equals(settings.getFraudProtectionEnabled())) {
            log.debug("Protection anti-fraude désactivée, validation ignorée");
            return;
        }
        
        Date weekStart = getWeekStartDate();
        long bookingsThisWeek;
        
        if (customerId != null) {
            bookingsThisWeek = bookingRepository.countBookingsByCustomerInPeriod(customerId, weekStart);
        } else if (email != null) {
            bookingsThisWeek = bookingRepository.countBookingsByEmailInPeriod(email, weekStart);
        } else {
            log.warn("Impossible de vérifier les limites: ni customerId ni email fourni");
            return;
        }
        
        Integer maxBookings = settings.getMaxBookingsPerWeek();
        
        log.info("Vérification limite réservations: {} cette semaine, max autorisé: {}", 
                bookingsThisWeek, maxBookings);
        
        if (bookingsThisWeek >= maxBookings) {
            log.warn("Limite de réservations atteinte pour le client {} (email: {}): {} >= {}", 
                    customerId, email, bookingsThisWeek, maxBookings);
            throw new SendByOpException(ErrorInfo.FRAUD_LIMIT_REACHED,
                    String.format("Vous avez atteint la limite de %d réservations par semaine. " +
                            "Veuillez réessayer la semaine prochaine.", maxBookings));
        }
    }

    /**
     * Vérifie si un utilisateur peut publier un nouveau voyage
     * @param customerId ID du client
     * @param email Email du client
     * @throws SendByOpException si la limite est atteinte
     */
    public void validateFlightLimit(Integer customerId, String email) throws SendByOpException {
        PlatformSettings settings = getSettings();
        
        if (!Boolean.TRUE.equals(settings.getFraudProtectionEnabled())) {
            log.debug("Protection anti-fraude désactivée, validation ignorée");
            return;
        }
        
        Date weekStart = getWeekStartDate();
        long flightsThisWeek;
        
        if (customerId != null) {
            flightsThisWeek = flightRepository.countFlightsByCustomerInPeriod(customerId, weekStart);
        } else if (email != null) {
            flightsThisWeek = flightRepository.countFlightsByEmailInPeriod(email, weekStart);
        } else {
            log.warn("Impossible de vérifier les limites: ni customerId ni email fourni");
            return;
        }
        
        Integer maxFlights = settings.getMaxFlightsPerWeek();
        
        log.info("Vérification limite voyages: {} cette semaine, max autorisé: {}", 
                flightsThisWeek, maxFlights);
        
        if (flightsThisWeek >= maxFlights) {
            log.warn("Limite de voyages atteinte pour le client {} (email: {}): {} >= {}", 
                    customerId, email, flightsThisWeek, maxFlights);
            throw new SendByOpException(ErrorInfo.FRAUD_LIMIT_REACHED,
                    String.format("Vous avez atteint la limite de %d voyages par semaine. " +
                            "Veuillez réessayer la semaine prochaine.", maxFlights));
        }
    }

    /**
     * Récupère le nombre de réservations restantes pour un utilisateur cette semaine
     * @param email Email du client
     * @return Nombre de réservations restantes
     */
    public int getRemainingBookings(String email) {
        PlatformSettings settings = getSettings();
        
        if (!Boolean.TRUE.equals(settings.getFraudProtectionEnabled())) {
            return Integer.MAX_VALUE;
        }
        
        Date weekStart = getWeekStartDate();
        long bookingsThisWeek = bookingRepository.countBookingsByEmailInPeriod(email, weekStart);
        int remaining = settings.getMaxBookingsPerWeek() - (int) bookingsThisWeek;
        
        return Math.max(0, remaining);
    }

    /**
     * Récupère le nombre de voyages restants pour un utilisateur cette semaine
     * @param email Email du client
     * @return Nombre de voyages restants
     */
    public int getRemainingFlights(String email) {
        PlatformSettings settings = getSettings();
        
        if (!Boolean.TRUE.equals(settings.getFraudProtectionEnabled())) {
            return Integer.MAX_VALUE;
        }
        
        Date weekStart = getWeekStartDate();
        long flightsThisWeek = flightRepository.countFlightsByEmailInPeriod(email, weekStart);
        int remaining = settings.getMaxFlightsPerWeek() - (int) flightsThisWeek;
        
        return Math.max(0, remaining);
    }

    /**
     * Récupère les limites actuelles configurées
     * @return DTO avec les limites
     */
    public FraudLimitsDto getCurrentLimits() {
        PlatformSettings settings = getSettings();
        return FraudLimitsDto.builder()
                .maxBookingsPerWeek(settings.getMaxBookingsPerWeek())
                .maxFlightsPerWeek(settings.getMaxFlightsPerWeek())
                .fraudProtectionEnabled(settings.getFraudProtectionEnabled())
                .build();
    }

    /**
     * Récupère les limites pour un utilisateur spécifique
     * @param email Email de l'utilisateur
     * @return DTO avec les limites et le nombre restant
     */
    public UserFraudStatusDto getUserFraudStatus(String email) {
        PlatformSettings settings = getSettings();
        Date weekStart = getWeekStartDate();
        
        long bookingsThisWeek = bookingRepository.countBookingsByEmailInPeriod(email, weekStart);
        long flightsThisWeek = flightRepository.countFlightsByEmailInPeriod(email, weekStart);
        
        return UserFraudStatusDto.builder()
                .email(email)
                .bookingsThisWeek((int) bookingsThisWeek)
                .flightsThisWeek((int) flightsThisWeek)
                .maxBookingsPerWeek(settings.getMaxBookingsPerWeek())
                .maxFlightsPerWeek(settings.getMaxFlightsPerWeek())
                .remainingBookings(Math.max(0, settings.getMaxBookingsPerWeek() - (int) bookingsThisWeek))
                .remainingFlights(Math.max(0, settings.getMaxFlightsPerWeek() - (int) flightsThisWeek))
                .fraudProtectionEnabled(settings.getFraudProtectionEnabled())
                .weekStartDate(weekStart)
                .build();
    }

    /**
     * Calcule la date de début de la semaine courante (Lundi 00:00)
     */
    private Date getWeekStartDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        // Si on est avant lundi, on recule d'une semaine
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.WEEK_OF_YEAR, -1);
        }
        
        return cal.getTime();
    }

    /**
     * Récupère les paramètres de la plateforme
     */
    private PlatformSettings getSettings() {
        return platformSettingsRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Aucun paramètre de plateforme trouvé, utilisation des valeurs par défaut");
                    PlatformSettings defaultSettings = new PlatformSettings();
                    defaultSettings.setMaxBookingsPerWeek(2);
                    defaultSettings.setMaxFlightsPerWeek(2);
                    defaultSettings.setFraudProtectionEnabled(true);
                    return defaultSettings;
                });
    }

    /**
     * DTO pour les limites de fraude
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class FraudLimitsDto {
        private Integer maxBookingsPerWeek;
        private Integer maxFlightsPerWeek;
        private Boolean fraudProtectionEnabled;
    }

    /**
     * DTO pour le statut anti-fraude d'un utilisateur
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UserFraudStatusDto {
        private String email;
        private Integer bookingsThisWeek;
        private Integer flightsThisWeek;
        private Integer maxBookingsPerWeek;
        private Integer maxFlightsPerWeek;
        private Integer remainingBookings;
        private Integer remainingFlights;
        private Boolean fraudProtectionEnabled;
        private Date weekStartDate;
    }
}
