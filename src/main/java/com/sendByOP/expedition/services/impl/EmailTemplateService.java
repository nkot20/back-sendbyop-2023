package com.sendByOP.expedition.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;
import java.util.Map;

/**
 * Service pour le rendu des templates d'emails avec Thymeleaf
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    /**
     * Génère le contenu HTML d'un email à partir d'un template Thymeleaf
     * 
     * @param templateName Nom du template (sans extension .html)
     * @param variables Variables à injecter dans le template
     * @return Contenu HTML généré
     */
    public String generateEmailContent(String templateName, Map<String, Object> variables) {
        log.debug("Génération du contenu email à partir du template: {}", templateName);
        
        try {
            Context context = new Context();
            
            // Ajouter les variables fournies
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            
            // Ajouter des variables communes à tous les emails
            context.setVariable("currentYear", Year.now().getValue());
            context.setVariable("websiteUrl", "https://www.sendbyop.com");
            
            String htmlContent = templateEngine.process("emails/" + templateName, context);
            log.debug("Contenu email généré avec succès pour le template: {}", templateName);
            
            return htmlContent;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du contenu email pour le template {}: {}", 
                     templateName, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du template email: " + templateName, e);
        }
    }

    /**
     * Génère un email de vérification
     * 
     * @param customerName Nom du client
     * @param verificationUrl URL de vérification complète
     * @return Contenu HTML de l'email
     */
    public String generateVerificationEmail(String customerName, String verificationUrl) {
        log.info("Génération de l'email de vérification pour: {}", customerName);
        
        Map<String, Object> variables = Map.of(
            "customerName", customerName,
            "verificationUrl", verificationUrl
        );
        
        return generateEmailContent("email-verification", variables);
    }

    /**
     * Génère un email de réinitialisation de mot de passe
     * 
     * @param customerName Nom du client
     * @param resetUrl URL de réinitialisation complète
     * @return Contenu HTML de l'email
     */
    public String generatePasswordResetEmail(String customerName, String resetUrl) {
        log.info("Génération de l'email de réinitialisation de mot de passe pour: {}", customerName);
        
        Map<String, Object> variables = Map.of(
            "customerName", customerName,
            "resetUrl", resetUrl
        );
        
        return generateEmailContent("password-reset", variables);
    }

    /**
     * Génère un email de bienvenue après vérification
     * 
     * @param customerName Nom du client
     * @return Contenu HTML de l'email
     */
    public String generateWelcomeEmail(String customerName) {
        log.info("Génération de l'email de bienvenue pour: {}", customerName);
        
        Map<String, Object> variables = Map.of(
            "customerName", customerName
        );
        
        return generateEmailContent("welcome", variables);
    }

    /**
     * Génère un email de confirmation de réservation
     */
    public String generateBookingConfirmation(Map<String, Object> bookingData) {
        log.info("Génération de l'email de confirmation de réservation");
        return generateEmailContent("booking-confirmation", bookingData);
    }

    /**
     * Génère un email de nouvelle réservation pour le voyageur
     */
    public String generateBookingPendingTraveler(Map<String, Object> bookingData) {
        log.info("Génération de l'email de nouvelle réservation pour le voyageur");
        return generateEmailContent("booking-pending-traveler", bookingData);
    }

    /**
     * Génère un email de rappel de paiement
     */
    public String generatePaymentReminder(Map<String, Object> paymentData) {
        log.info("Génération de l'email de rappel de paiement");
        return generateEmailContent("payment-reminder", paymentData);
    }

    /**
     * Génère un email de notification de livraison
     */
    public String generateDeliveryNotification(Map<String, Object> deliveryData) {
        log.info("Génération de l'email de notification de livraison");
        return generateEmailContent("delivery-notification", deliveryData);
    }

    /**
     * Génère un email d'avis d'annulation
     */
    public String generateCancellationNotice(Map<String, Object> cancellationData) {
        log.info("Génération de l'email d'avis d'annulation");
        return generateEmailContent("cancellation-notice", cancellationData);
    }

    /**
     * Génère un email de confirmation de récupération pour le client
     */
    public String generatePickupConfirmationCustomer(Map<String, Object> pickupData) {
        log.info("Génération de l'email de confirmation de récupération pour le client");
        return generateEmailContent("pickup-confirmation-customer", pickupData);
    }

    /**
     * Génère un email de confirmation de récupération pour le voyageur
     */
    public String generatePickupConfirmationTraveler(Map<String, Object> pickupData) {
        log.info("Génération de l'email de confirmation de récupération pour le voyageur");
        return generateEmailContent("pickup-confirmation-traveler", pickupData);
    }
}
