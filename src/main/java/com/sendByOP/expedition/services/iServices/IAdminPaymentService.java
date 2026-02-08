package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.AdminPaymentDto;
import com.sendByOP.expedition.models.dto.AdminPayoutDto;
import com.sendByOP.expedition.models.enums.PayoutStatus;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service pour la gestion des paiements et versements dans le panel d'administration
 */
public interface IAdminPaymentService {
    
    /**
     * Récupère tous les paiements avec pagination
     */
    Page<AdminPaymentDto> getAllPayments(Pageable pageable);
    
    /**
     * Récupère les paiements par statut
     */
    Page<AdminPaymentDto> getPaymentsByStatus(TransactionStatus status, Pageable pageable);
    
    /**
     * Recherche des paiements
     */
    Page<AdminPaymentDto> searchPayments(String search, Pageable pageable);
    
    /**
     * Récupère un paiement par ID
     */
    AdminPaymentDto getPaymentById(Integer id);
    
    /**
     * Récupère tous les versements avec pagination
     */
    Page<AdminPayoutDto> getAllPayouts(Pageable pageable);
    
    /**
     * Récupère les versements par statut
     */
    Page<AdminPayoutDto> getPayoutsByStatus(PayoutStatus status, Pageable pageable);
    
    /**
     * Recherche des versements
     */
    Page<AdminPayoutDto> searchPayouts(String search, Pageable pageable);
    
    /**
     * Récupère un versement par ID
     */
    AdminPayoutDto getPayoutById(Long id);
    
    /**
     * Traiter un versement (marquer comme payé)
     */
    AdminPayoutDto processPayou(Long id, String transactionId, String paymentMethod);
}
