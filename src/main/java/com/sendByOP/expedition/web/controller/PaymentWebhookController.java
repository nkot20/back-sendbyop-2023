package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.WebhookPaymentNotification;
import com.sendByOP.expedition.models.enums.PaymentMethod;
import com.sendByOP.expedition.services.impl.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour recevoir les webhooks de paiement des différents providers
 * IMPORTANT: Ces endpoints doivent être accessibles sans authentification
 */
@RestController
@RequestMapping("/webhooks/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {
    
    private final PaymentService paymentService;
    
    /**
     * Webhook pour Orange Money
     */
    @PostMapping("/orange-money")
    public ResponseEntity<Map<String, Object>> orangeMoneyWebhook(
            @RequestBody WebhookPaymentNotification notification,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        
        log.info("Webhook reçu de Orange Money - Transaction: {}", 
                notification.getTransactionReference());
        
        return processWebhook(notification, PaymentMethod.ORANGE_MONEY, signature);
    }
    
    /**
     * Webhook pour MTN Mobile Money
     */
    @PostMapping("/mtn-mobile-money")
    public ResponseEntity<Map<String, Object>> mtnMobileMoneyWebhook(
            @RequestBody WebhookPaymentNotification notification,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        
        log.info("Webhook reçu de MTN Mobile Money - Transaction: {}", 
                notification.getTransactionReference());
        
        return processWebhook(notification, PaymentMethod.MTN_MOBILE_MONEY, signature);
    }
    
    /**
     * Webhook pour paiements par carte
     */
    @PostMapping("/credit-card")
    public ResponseEntity<Map<String, Object>> creditCardWebhook(
            @RequestBody WebhookPaymentNotification notification,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        
        log.info("Webhook reçu pour paiement Carte - Transaction: {}", 
                notification.getTransactionReference());
        
        return processWebhook(notification, PaymentMethod.CREDIT_CARD, signature);
    }
    
    /**
     * Webhook pour PayPal
     */
    @PostMapping("/paypal")
    public ResponseEntity<Map<String, Object>> paypalWebhook(
            @RequestBody WebhookPaymentNotification notification,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        
        log.info("Webhook reçu de PayPal - Transaction: {}", 
                notification.getTransactionReference());
        
        return processWebhook(notification, PaymentMethod.PAYPAL, signature);
    }
    
    /**
     * Endpoint de simulation pour tester les webhooks
     * À SUPPRIMER EN PRODUCTION
     */
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateWebhook(
            @RequestParam String transactionReference,
            @RequestParam String status,
            @RequestParam String paymentMethod) {
        
        log.warn("⚠️  SIMULATION DE WEBHOOK - À SUPPRIMER EN PRODUCTION");
        log.info("Simulation webhook - Transaction: {}, Status: {}, Method: {}", 
                transactionReference, status, paymentMethod);
        
        try {
            WebhookPaymentNotification notification = WebhookPaymentNotification.builder()
                    .transactionReference(transactionReference)
                    .status(status)
                    .externalTransactionId("SIM-" + System.currentTimeMillis())
                    .message("Paiement simulé")
                    .build();
            
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            String signature = "SIMULATED_SIGNATURE";
            
            return processWebhook(notification, method, signature);
            
        } catch (Exception e) {
            log.error("Erreur lors de la simulation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Erreur lors de la simulation: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Méthode privée pour traiter tous les webhooks de manière uniforme
     */
    private ResponseEntity<Map<String, Object>> processWebhook(
            WebhookPaymentNotification notification,
            PaymentMethod paymentMethod,
            String signature) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Valider les données du webhook
            if (notification.getTransactionReference() == null || 
                notification.getTransactionReference().isEmpty()) {
                log.error("Référence de transaction manquante dans le webhook");
                response.put("success", false);
                response.put("message", "Référence de transaction manquante");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Traiter le webhook via le service de paiement
            paymentService.processWebhookNotification(notification, paymentMethod, signature);
            
            log.info("Webhook traité avec succès - Transaction: {}", 
                    notification.getTransactionReference());
            
            response.put("success", true);
            response.put("message", "Webhook traité avec succès");
            return ResponseEntity.ok(response);
            
        } catch (SendByOpException e) {
            log.error("Erreur lors du traitement du webhook: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            log.error("Erreur inattendue lors du traitement du webhook: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
