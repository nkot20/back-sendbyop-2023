package com.sendByOP.expedition.scheduling;

import com.sendByOP.expedition.services.iServices.IBookingService;
import com.sendByOP.expedition.services.iServices.IPayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler pour les tâches automatisées liées aux réservations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final IBookingService bookingService;
    private final IPayoutService payoutService;

    /**
     * Annule automatiquement les réservations non payées dont la deadline est dépassée
     * 
     * Exécution: Toutes les 10 minutes
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void autoCancelUnpaidBookings() {
        log.info("Starting auto-cancellation job for unpaid bookings");
        
        try {
            int cancelledCount = bookingService.autoCancelUnpaidBookings();
            
            if (cancelledCount > 0) {
                log.warn("Auto-cancelled {} unpaid booking(s) with expired deadline", cancelledCount);
            } else {
                log.debug("No unpaid bookings to cancel");
            }
        } catch (Exception e) {
            log.error("Error during auto-cancellation job", e);
        }
        
        log.info("Auto-cancellation job completed");
    }

    /**
     * Traite les paiements automatiques aux voyageurs pour les réservations complétées
     * 
     * Exécution: Tous les jours à 2h du matin
     * Cron: "0 0 2 * * *" = À 2:00:00 chaque jour
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void autoPayoutToTravelers() {
        log.info("Starting auto-payout job for completed bookings");
        
        try {
            int payoutCount = payoutService.processAutomaticPayouts();
            
            if (payoutCount > 0) {
                log.info("Processed {} automatic payout(s)", payoutCount);
            } else {
                log.debug("No completed bookings to process for payout");
            }
            
            log.info("Auto-payout job completed");
        } catch (Exception e) {
            log.error("Error during auto-payout job", e);
        }
    }
}
