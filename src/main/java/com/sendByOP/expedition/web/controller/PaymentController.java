package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.InitiatePaymentRequest;
import com.sendByOP.expedition.models.dto.PaymentResponse;
import com.sendByOP.expedition.services.impl.PaymentService;
import com.sendByOP.expedition.services.impl.InvoiceService;
import com.sendByOP.expedition.models.entities.Transaction;
import com.sendByOP.expedition.repositories.TransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des paiements
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final TransactionRepository transactionRepository;
    
    /**
     * Initie un nouveau paiement
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            Principal principal) {
        
        log.info("Requête d'initiation de paiement de {} pour la réservation {}", 
                principal.getName(), request.getBookingId());
        
        try {
            // S'assurer que l'email correspond à l'utilisateur connecté
            request.setCustomerEmail(principal.getName());
            
            PaymentResponse response = paymentService.initiatePayment(request);
            
            log.info("Paiement initié avec succès - Transaction: {}", 
                    response.getTransactionReference());
            
            return ResponseEntity.ok(response);
            
        } catch (SendByOpException e) {
            log.error("Erreur lors de l'initiation du paiement: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'initiation du paiement: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Vérifie le statut d'un paiement
     */
    @GetMapping("/status/{transactionReference}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> checkPaymentStatus(
            @PathVariable String transactionReference,
            Principal principal) {
        
        log.info("Vérification du statut du paiement: {} par {}", 
                transactionReference, principal.getName());
        
        try {
            PaymentResponse response = paymentService.checkPaymentStatus(transactionReference);
            
            log.info("Statut du paiement récupéré: {}", response.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (SendByOpException e) {
            log.error("Erreur lors de la vérification du statut: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la vérification du statut: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Récupère l'historique des transactions d'une réservation
     */
    @GetMapping("/booking/{bookingId}/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getBookingTransactions(
            @PathVariable Integer bookingId,
            Principal principal) {
        
        log.info("Récupération des transactions pour la réservation: {} par {}", 
                bookingId, principal.getName());
        
        try {
            List<PaymentResponse> transactions = paymentService.getBookingTransactions(bookingId);
            
            log.info("{} transaction(s) trouvée(s) pour la réservation {}", 
                    transactions.size(), bookingId);
            
            return ResponseEntity.ok(transactions);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des transactions: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Une erreur s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Récupère l'historique des paiements du client (paginé)
     */
    @GetMapping("/history/paginated")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getPaymentHistoryPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        
        log.info("Récupération de l'historique paginé des paiements pour {}", principal.getName());
        
        try {
            return ResponseEntity.ok(paymentService.getPaymentHistory(principal.getName(), page, size));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'historique: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Une erreur s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Récupère l'historique complet des paiements du client
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getPaymentHistory(Principal principal) {
        
        log.info("Récupération de l'historique complet des paiements pour {}", principal.getName());
        
        try {
            return ResponseEntity.ok(paymentService.getPaymentHistoryAll(principal.getName()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'historique: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Une erreur s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Récupère les statistiques de paiement du client
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getPaymentStats(Principal principal) {
        
        log.info("Récupération des statistiques de paiement pour {}", principal.getName());
        
        try {
            return ResponseEntity.ok(paymentService.getPaymentStats(principal.getName()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Une erreur s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Endpoint de test pour vérifier que l'API de paiement fonctionne
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Payment API");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
    
    /**
     * Télécharge la facture/reçu pour une transaction
     */
    @GetMapping("/{transactionId}/invoice")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Integer transactionId,
            Principal principal) {
        
        log.info("Téléchargement de la facture pour la transaction: {} par {}", 
                transactionId, principal.getName());
        
        try {
            // Récupérer la transaction
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Transaction non trouvée"));
            
            // Vérifier que la transaction appartient bien au client
            if (!transaction.getCustomer().getEmail().equals(principal.getName())) {
                log.warn("Tentative d'accès non autorisé à la facture {} par {}", 
                        transactionId, principal.getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Générer la facture
            byte[] invoicePdf = invoiceService.generateInvoice(transaction);
            
            // Préparer les headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "facture_" + transaction.getTransactionReference() + ".pdf");
            headers.setContentLength(invoicePdf.length);
            
            log.info("Facture générée et prête au téléchargement pour la transaction: {}", 
                    transactionId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(invoicePdf);
            
        } catch (SendByOpException e) {
            log.error("Erreur lors du téléchargement de la facture: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            
        } catch (Exception e) {
            log.error("Erreur inattendue lors du téléchargement de la facture: {}", 
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
