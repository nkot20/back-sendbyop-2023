package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.services.iServices.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'implémentation des notifications
 * Utilise des templates Thymeleaf pour générer des emails HTML professionnels
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final SendMailService emailService;
    private final EmailTemplateService templateService;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Formate une java.util.Date en String
     */
    private String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : "";
    }
    
    /**
     * Formate une LocalDateTime en String (date seulement)
     */
    private String formatLocalDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }
    
    /**
     * Formate une LocalDateTime en String (heure seulement)
     */
    private String formatLocalTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : "";
    }

    @Override
    public void sendBookingConfirmation(Booking booking) {
        log.info("Sending booking confirmation email to {}", booking.getCustomer().getEmail());
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName());
            variables.put("bookingId", booking.getId());
            variables.put("departureCity", booking.getFlight().getDepartureAirport().getCity().getName());
            variables.put("arrivalCity", booking.getFlight().getArrivalAirport().getCity().getName());
            variables.put("departureDate", formatDate(booking.getFlight().getDepartureDate()));
            variables.put("totalPrice", booking.getTotalPrice());
            variables.put("status", booking.getStatus().getDisplayName());
            
            String htmlContent = templateService.generateBookingConfirmation(variables);
            
            emailService.sendHtmlEmail(
                    booking.getCustomer().getEmail(),
                    "Confirmation de réservation #" + booking.getId(),
                    htmlContent
            );
            
            log.info("Booking confirmation email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email: {}", e.getMessage());
        }
    }

    @Override
    public void sendBookingPendingToTraveler(Booking booking) {
        log.info("Sending new booking notification to traveler");
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("travelerName", booking.getFlight().getCustomer().getFirstName() + " " + 
                                          booking.getFlight().getCustomer().getLastName());
            variables.put("customerName", booking.getCustomer().getFirstName() + " " + 
                                         booking.getCustomer().getLastName());
            variables.put("bookingId", booking.getId());
            variables.put("departureCity", booking.getFlight().getDepartureAirport().getCity().getName());
            variables.put("arrivalCity", booking.getFlight().getArrivalAirport().getCity().getName());
            variables.put("totalPrice", booking.getTotalPrice());
            
            String htmlContent = templateService.generateBookingPendingTraveler(variables);
            
            emailService.sendHtmlEmail(
                    booking.getFlight().getCustomer().getEmail(),
                    "Nouvelle réservation #" + booking.getId(),
                    htmlContent
            );
            
            log.info("Booking pending email sent successfully to traveler");
        } catch (Exception e) {
            log.error("Failed to send booking pending email: {}", e.getMessage());
        }
    }

    @Override
    public void sendPaymentReminder(Booking booking, int hoursRemaining) {
        log.info("Sending payment reminder");
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName());
            variables.put("bookingId", booking.getId());
            variables.put("hoursRemaining", hoursRemaining);
            variables.put("isUrgent", hoursRemaining <= 2);
            variables.put("totalPrice", booking.getTotalPrice());
            variables.put("deadlineDate", formatLocalDate(booking.getPaymentDeadline()));
            variables.put("deadlineTime", formatLocalTime(booking.getPaymentDeadline()));
            
            String htmlContent = templateService.generatePaymentReminder(variables);
            
            emailService.sendHtmlEmail(
                    booking.getCustomer().getEmail(),
                    "⚠️ Rappel de paiement - Réservation #" + booking.getId(),
                    htmlContent
            );
            
            log.info("Payment reminder sent successfully");
        } catch (Exception e) {
            log.error("Failed to send payment reminder: {}", e.getMessage());
        }
    }

    @Override
    public void sendDeliveryNotification(Booking booking) {
        log.info("Sending delivery notification");
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName());
            variables.put("bookingId", booking.getId());
            variables.put("receiverName", booking.getReceiver().getFirstName() + " " + booking.getReceiver().getLastName());
            variables.put("deliveryDate", formatLocalDate(booking.getDeliveredAt()));
            variables.put("deliveryTime", formatLocalTime(booking.getDeliveredAt()));
            
            String htmlContent = templateService.generateDeliveryNotification(variables);
            
            emailService.sendHtmlEmail(
                    booking.getCustomer().getEmail(),
                    "✓ Colis livré - Réservation #" + booking.getId(),
                    htmlContent
            );
            
            log.info("Delivery notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send delivery notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendCancellationNotice(Booking booking, String reason) {
        log.info("Sending cancellation notice");
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName());
            variables.put("bookingId", booking.getId());
            variables.put("reason", reason != null && !reason.isEmpty() ? reason : "Non spécifiée");
            variables.put("status", booking.getStatus().getDisplayName());
            
            String htmlContent = templateService.generateCancellationNotice(variables);
            
            emailService.sendHtmlEmail(
                    booking.getCustomer().getEmail(),
                    "Annulation de réservation #" + booking.getId(),
                    htmlContent
            );
            
            log.info("Cancellation notice sent successfully");
        } catch (Exception e) {
            log.error("Failed to send cancellation notice: {}", e.getMessage());
        }
    }

    @Override
    public void sendPickupConfirmation(Booking booking) {
        log.info("Sending pickup confirmations");
        
        // Email au client
        try {
            Map<String, Object> customerVariables = new HashMap<>();
            customerVariables.put("customerName", booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName());
            customerVariables.put("bookingId", booking.getId());
            customerVariables.put("pickupDate", formatLocalDate(booking.getPickedUpAt()));
            customerVariables.put("pickupTime", formatLocalTime(booking.getPickedUpAt()));
            
            String customerHtml = templateService.generatePickupConfirmationCustomer(customerVariables);
            
            emailService.sendHtmlEmail(
                    booking.getCustomer().getEmail(),
                    "✓ Colis récupéré - Réservation #" + booking.getId(),
                    customerHtml
            );
            
            log.info("Pickup confirmation sent to customer");
        } catch (Exception e) {
            log.error("Failed to send pickup confirmation to customer: {}", e.getMessage());
        }
        
        // Email au voyageur
        try {
            Map<String, Object> travelerVariables = new HashMap<>();
            travelerVariables.put("travelerName", booking.getFlight().getCustomer().getFirstName() + " " + 
                                                  booking.getFlight().getCustomer().getLastName());
            travelerVariables.put("bookingId", booking.getId());
            travelerVariables.put("pickupDate", formatLocalDate(booking.getPickedUpAt()));
            travelerVariables.put("pickupTime", formatLocalTime(booking.getPickedUpAt()));
            travelerVariables.put("totalPrice", booking.getTotalPrice());
            
            String travelerHtml = templateService.generatePickupConfirmationTraveler(travelerVariables);
            
            emailService.sendHtmlEmail(
                    booking.getFlight().getCustomer().getEmail(),
                    "✓ Livraison confirmée - Réservation #" + booking.getId(),
                    travelerHtml
            );
            
            log.info("Pickup confirmation sent to traveler");
        } catch (Exception e) {
            log.error("Failed to send pickup confirmation to traveler: {}", e.getMessage());
        }
    }
}
