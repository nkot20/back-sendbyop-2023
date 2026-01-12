package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.dto.BookingStatsDto;
import com.sendByOP.expedition.models.dto.RevenueStatsDto;
import com.sendByOP.expedition.models.dto.UserStatsDto;
import com.sendByOP.expedition.models.enums.BookingStatus;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.repositories.CustomerRepository;
import com.sendByOP.expedition.services.iServices.IStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'implémentation des statistiques
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService implements IStatisticsService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

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
            totalRevenue = totalRevenue.add(booking.getTotalPrice());
        }
        for (var booking : deliveredBookings) {
            totalRevenue = totalRevenue.add(booking.getTotalPrice());
        }
        for (var booking : pickedUpBookings) {
            totalRevenue = totalRevenue.add(booking.getTotalPrice());
        }

        // Revenu en attente (confirmées non payées)
        var unpaidBookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED_UNPAID);
        BigDecimal pendingRevenue = BigDecimal.ZERO;
        for (var booking : unpaidBookings) {
            pendingRevenue = pendingRevenue.add(booking.getTotalPrice());
        }

        // Commission (exemple: 10% de la plateforme)
        BigDecimal commissionRate = new BigDecimal("0.10");
        BigDecimal totalCommission = totalRevenue.multiply(commissionRate);

        // Montant aux voyageurs
        BigDecimal totalPaidToTravelers = totalRevenue.subtract(totalCommission);
        
        // En attente de payout (PICKED_UP seulement)
        BigDecimal pendingPayout = BigDecimal.ZERO;
        for (var booking : pickedUpBookings) {
            BigDecimal bookingPayout = booking.getTotalPrice()
                    .multiply(BigDecimal.ONE.subtract(commissionRate));
            pendingPayout = pendingPayout.add(bookingPayout);
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

        // Compter tous les utilisateurs
        long totalUsers = customerRepository.count();

        // Utilisateurs actifs (ayant au moins une réservation)
        // TODO: Optimiser avec requête custom
        long activeUsers = totalUsers; // Placeholder

        // Statistiques basiques
        return UserStatsDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .newUsers(0L) // TODO: Filtrer par date création
                .travelers(0L) // TODO: Compter customers avec vols
                .senders(0L) // TODO: Compter customers avec réservations
                .activeUserRate(totalUsers > 0 ? 100.0 : 0.0)
                .averageBookingsPerUser(0.0) // TODO: Calculer moyenne
                .build();
    }
}
