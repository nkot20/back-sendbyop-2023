package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.dto.BookingStatsDto;
import com.sendByOP.expedition.models.dto.RecentActivityDto;
import com.sendByOP.expedition.models.dto.RevenueStatsDto;
import com.sendByOP.expedition.models.dto.UserStatsDto;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.enums.BookingStatus;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.repositories.CustomerRepository;
import com.sendByOP.expedition.repositories.FlightRepository;
import com.sendByOP.expedition.repositories.UserRepository;
import com.sendByOP.expedition.services.iServices.IStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'implémentation des statistiques
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService implements IStatisticsService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;

    @Override
    public BookingStatsDto getBookingStatistics(LocalDate from, LocalDate to) {
        log.info("Fetching booking statistics from {} to {}", from, to);

        // Compter toutes les réservations
        long totalBookings = bookingRepository.count();

        // Compter par statut
        long pendingConfirmation = bookingRepository.countByStatus(BookingStatus.PENDING_CONFIRMATION);
        long confirmedUnpaid = bookingRepository.countByStatus(BookingStatus.CONFIRMED_UNPAID);
        long confirmedPaid = bookingRepository.countByStatus(BookingStatus.CONFIRMED_PAID);
        long delivered = bookingRepository.countByStatus(BookingStatus.DELIVERED);
        long pickedUp = bookingRepository.countByStatus(BookingStatus.PICKED_UP);
        long cancelledByClient = bookingRepository.countByStatus(BookingStatus.CANCELLED_BY_CLIENT);
        long cancelledByTraveler = bookingRepository.countByStatus(BookingStatus.CANCELLED_BY_TRAVELER);
        long cancelledPaymentTimeout = bookingRepository.countByStatus(BookingStatus.CANCELLED_PAYMENT_TIMEOUT);

        long cancelled = cancelledByClient + cancelledByTraveler + cancelledPaymentTimeout;

        // Créer map des statuts
        Map<String, Long> bookingsByStatus = new HashMap<>();
        bookingsByStatus.put("PENDING_CONFIRMATION", pendingConfirmation);
        bookingsByStatus.put("CONFIRMED_UNPAID", confirmedUnpaid);
        bookingsByStatus.put("CONFIRMED_PAID", confirmedPaid);
        bookingsByStatus.put("DELIVERED", delivered);
        bookingsByStatus.put("PICKED_UP", pickedUp);
        bookingsByStatus.put("CANCELLED_BY_CLIENT", cancelledByClient);
        bookingsByStatus.put("CANCELLED_BY_TRAVELER", cancelledByTraveler);
        bookingsByStatus.put("CANCELLED_PAYMENT_TIMEOUT", cancelledPaymentTimeout);

        // Calculer taux
        double conversionRate = totalBookings > 0 
                ? (double) pickedUp / totalBookings * 100 
                : 0.0;
        double cancellationRate = totalBookings > 0 
                ? (double) cancelled / totalBookings * 100 
                : 0.0;

        return BookingStatsDto.builder()
                .totalBookings(totalBookings)
                .bookingsByStatus(bookingsByStatus)
                .pendingConfirmation(pendingConfirmation)
                .confirmedUnpaid(confirmedUnpaid)
                .confirmedPaid(confirmedPaid)
                .delivered(delivered)
                .pickedUp(pickedUp)
                .cancelled(cancelled)
                .cancelledByClient(cancelledByClient)
                .cancelledByTraveler(cancelledByTraveler)
                .cancelledPaymentTimeout(cancelledPaymentTimeout)
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public RevenueStatsDto getRevenueStatistics(LocalDate from, LocalDate to) {
        log.info("Fetching revenue statistics from {} to {}", from, to);

        // Récupérer toutes les réservations payées
        var paidBookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED_PAID);
        var deliveredBookings = bookingRepository.findByStatus(BookingStatus.DELIVERED);
        var pickedUpBookings = bookingRepository.findByStatus(BookingStatus.PICKED_UP);

        // Calculer revenus
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (var booking : paidBookings) {
            if (booking.getTotalPrice() != null) {
                totalRevenue = totalRevenue.add(booking.getTotalPrice());
            }
        }
        for (var booking : deliveredBookings) {
            if (booking.getTotalPrice() != null) {
                totalRevenue = totalRevenue.add(booking.getTotalPrice());
            }
        }
        for (var booking : pickedUpBookings) {
            if (booking.getTotalPrice() != null) {
                totalRevenue = totalRevenue.add(booking.getTotalPrice());
            }
        }

        // Revenu en attente (confirmées non payées)
        var unpaidBookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED_UNPAID);
        BigDecimal pendingRevenue = BigDecimal.ZERO;
        for (var booking : unpaidBookings) {
            if (booking.getTotalPrice() != null) {
                pendingRevenue = pendingRevenue.add(booking.getTotalPrice());
            }
        }

        // Commission (exemple: 10% de la plateforme)
        BigDecimal commissionRate = new BigDecimal("0.10");
        BigDecimal totalCommission = totalRevenue.multiply(commissionRate);

        // Montant aux voyageurs
        BigDecimal totalPaidToTravelers = totalRevenue.subtract(totalCommission);
        
        // En attente de payout (PICKED_UP seulement)
        BigDecimal pendingPayout = BigDecimal.ZERO;
        for (var booking : pickedUpBookings) {
            if (booking.getTotalPrice() != null) {
                BigDecimal bookingPayout = booking.getTotalPrice()
                        .multiply(BigDecimal.ONE.subtract(commissionRate));
                pendingPayout = pendingPayout.add(bookingPayout);
            }
        }

        // Revenu moyen
        long paidCount = paidBookings.size() + deliveredBookings.size() + pickedUpBookings.size();
        BigDecimal averageRevenue = paidCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(paidCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RevenueStatsDto.builder()
                .totalRevenue(totalRevenue)
                .pendingRevenue(pendingRevenue)
                .totalCommission(totalCommission)
                .totalPaidToTravelers(totalPaidToTravelers)
                .pendingPayoutToTravelers(pendingPayout)
                .averageRevenuePerBooking(averageRevenue)
                .paidBookingsCount((long) paidCount)
                .averageCommissionRate(commissionRate.multiply(BigDecimal.valueOf(100)).doubleValue())
                .lostRevenue(BigDecimal.ZERO) // TODO: Calculer depuis annulations après paiement
                .build();
    }

    @Override
    public UserStatsDto getUserStatistics() {
        log.info("Fetching user statistics");

        // Compter tous les utilisateurs depuis la table User
        long totalUsers = userRepository.count();

        // Calculer les nouveaux utilisateurs (dernière semaine)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        long newUsers = userRepository.findAll().stream()
                .filter(user -> {
                    if (user.getCreatedAt() == null) return false;
                    // Convertir Date en LocalDateTime pour comparaison
                    LocalDateTime createdAt = user.getCreatedAt().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();
                    return createdAt.isAfter(oneWeekAgo);
                })
                .count();

        // Utilisateurs avec au moins une réservation (actifs)
        Set<Long> activeUserIds = new HashSet<>();
        for (var booking : bookingRepository.findAll()) {
            if (booking.getCustomer() != null) {
                activeUserIds.add(booking.getCustomer().getId().longValue());
            }
        }
        long activeUsers = activeUserIds.size();

        // Compter les voyageurs (customers avec au moins un vol)
        long travelers = flightRepository.findAll().stream()
                .filter(flight -> flight.getCustomer() != null)
                .map(flight -> flight.getCustomer().getId())
                .distinct()
                .count();

        // Compter les expéditeurs (customers avec au moins une réservation)
        long senders = bookingRepository.findAll().stream()
                .filter(booking -> booking.getCustomer() != null)
                .map(booking -> booking.getCustomer().getId())
                .distinct()
                .count();

        // Calculer taux d'utilisateurs actifs
        double activeUserRate = totalUsers > 0 
                ? (double) activeUsers / totalUsers * 100 
                : 0.0;

        // Calculer nombre moyen de réservations par utilisateur actif
        long totalBookings = bookingRepository.count();
        double averageBookingsPerUser = activeUsers > 0 
                ? (double) totalBookings / activeUsers 
                : 0.0;

        return UserStatsDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .newUsers(newUsers)
                .travelers(travelers)
                .senders(senders)
                .activeUserRate(Math.round(activeUserRate * 100.0) / 100.0)
                .averageBookingsPerUser(Math.round(averageBookingsPerUser * 100.0) / 100.0)
                .build();
    }

    @Override
    public List<RecentActivityDto> getRecentActivity(int limit) {
        log.info("Fetching recent activity, limit: {}", limit);
        
        List<RecentActivityDto> activities = new ArrayList<>();
        
        // Récupérer les réservations récentes
        List<Booking> recentBookings = bookingRepository.findAll().stream()
                .sorted((b1, b2) -> {
                    LocalDateTime t1 = b1.getCreatedAt() != null 
                        ? b1.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : LocalDateTime.MIN;
                    LocalDateTime t2 = b2.getCreatedAt() != null 
                        ? b2.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : LocalDateTime.MIN;
                    return t2.compareTo(t1);
                })
                .limit(limit / 2)
                .collect(Collectors.toList());
        
        for (Booking booking : recentBookings) {
            String userName = booking.getCustomer() != null 
                ? booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName()
                : "Utilisateur inconnu";
            String userEmail = booking.getCustomer() != null 
                ? booking.getCustomer().getEmail()
                : "";
            
            LocalDateTime timestamp = booking.getCreatedAt() != null
                ? booking.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();
            
            String action = "Nouvelle réservation";
            String description = String.format("Réservation #%d créée", booking.getId());
            
            if (booking.getStatus() == BookingStatus.CONFIRMED_PAID) {
                action = "Paiement confirmé";
                description = String.format("Paiement confirmé pour la réservation #%d", booking.getId());
            } else if (booking.getStatus() == BookingStatus.DELIVERED) {
                action = "Colis livré";
                description = String.format("Colis de la réservation #%d livré", booking.getId());
            } else if (booking.getStatus() == BookingStatus.CANCELLED_BY_CLIENT || 
                       booking.getStatus() == BookingStatus.CANCELLED_BY_TRAVELER) {
                action = "Réservation annulée";
                description = String.format("Réservation #%d annulée", booking.getId());
            }
            
            activities.add(RecentActivityDto.builder()
                    .type("BOOKING")
                    .action(action)
                    .description(description)
                    .userName(userName)
                    .userEmail(userEmail)
                    .entityId(booking.getId().longValue())
                    .timestamp(timestamp)
                    .build());
        }
        
        // Récupérer les vols récents
        flightRepository.findAll().stream()
                .sorted((f1, f2) -> {
                    LocalDateTime t1 = f1.getCreatedAt() != null 
                        ? f1.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : LocalDateTime.MIN;
                    LocalDateTime t2 = f2.getCreatedAt() != null 
                        ? f2.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : LocalDateTime.MIN;
                    return t2.compareTo(t1);
                })
                .limit(limit / 3)
                .forEach(flight -> {
                    String userName = flight.getCustomer() != null 
                        ? flight.getCustomer().getFirstName() + " " + flight.getCustomer().getLastName()
                        : "Voyageur inconnu";
                    String userEmail = flight.getCustomer() != null 
                        ? flight.getCustomer().getEmail()
                        : "";
                    
                    LocalDateTime timestamp = flight.getCreatedAt() != null
                        ? flight.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : LocalDateTime.now();
                    
                    String action = "Voyage publié";
                    String description = String.format("Nouveau voyage #%d publié", flight.getFlightId());
                    
                    if (flight.getValidationStatus() == 1) {
                        action = "Voyage validé";
                        description = String.format("Voyage #%d validé par l'admin", flight.getFlightId());
                    } else if (flight.getValidationStatus() == 2) {
                        action = "Voyage rejeté";
                        description = String.format("Voyage #%d rejeté", flight.getFlightId());
                    }
                    
                    activities.add(RecentActivityDto.builder()
                            .type("TRIP")
                            .action(action)
                            .description(description)
                            .userName(userName)
                            .userEmail(userEmail)
                            .entityId(flight.getFlightId().longValue())
                            .timestamp(timestamp)
                            .build());
                });
        
        // Récupérer les nouveaux utilisateurs
        userRepository.findAll().stream()
                .sorted((u1, u2) -> {
                    LocalDateTime t1 = u1.getCreatedAt() != null 
                        ? u1.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : LocalDateTime.MIN;
                    LocalDateTime t2 = u2.getCreatedAt() != null 
                        ? u2.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : LocalDateTime.MIN;
                    return t2.compareTo(t1);
                })
                .limit(limit / 4)
                .forEach(user -> {
                    String userName = user.getFirstName() + " " + user.getLastName();
                    String userEmail = user.getEmail();
                    
                    LocalDateTime timestamp = user.getCreatedAt() != null
                        ? user.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : LocalDateTime.now();
                    
                    activities.add(RecentActivityDto.builder()
                            .type("USER")
                            .action("Nouvel utilisateur")
                            .description(String.format("Inscription de %s", userName))
                            .userName(userName)
                            .userEmail(userEmail)
                            .entityId(user.getId().longValue())
                            .timestamp(timestamp)
                            .build());
                });
        
        // Trier toutes les activités par date décroissante et limiter
        return activities.stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
