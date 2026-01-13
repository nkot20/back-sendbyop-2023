package com.sendByOP.expedition.services.payment;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.InitiatePaymentRequest;
import com.sendByOP.expedition.models.dto.PaymentResponse;
import com.sendByOP.expedition.models.dto.WebhookPaymentNotification;
import com.sendByOP.expedition.models.entities.Transaction;
import com.sendByOP.expedition.models.enums.PaymentMethod;

/**
 * Interface abstraite pour tous les providers de paiement.
 * Chaque méthode de paiement (Orange Money, MTN, Carte, PayPal) implémente cette interface.
 */
public interface PaymentProvider {
    
    /**
     * Retourne la méthode de paiement supportée par ce provider
     */
    PaymentMethod getPaymentMethod();
    
    /**
     * Initie un paiement auprès du provider
     * 
     * @param request Détails de la demande de paiement
     * @param transaction Transaction en base de données
     * @return Réponse avec les détails du paiement initié
     * @throws SendByOpException Si une erreur se produit
     */
    PaymentResponse initiatePayment(InitiatePaymentRequest request, Transaction transaction) 
            throws SendByOpException;
    
    /**
     * Vérifie le statut d'un paiement auprès du provider
     * 
     * @param transaction Transaction à vérifier
     * @return Réponse avec le statut actuel
     * @throws SendByOpException Si une erreur se produit
     */
    PaymentResponse checkPaymentStatus(Transaction transaction) throws SendByOpException;
    
    /**
     * Traite une notification webhook du provider
     * 
     * @param notification Notification reçue du provider
     * @return Transaction reference si le webhook est valide
     * @throws SendByOpException Si le webhook est invalide
     */
    String processWebhookNotification(WebhookPaymentNotification notification) 
            throws SendByOpException;
    
    /**
     * Vérifie si le webhook reçu est authentique (signature, etc.)
     * 
     * @param notification Notification à vérifier
     * @param signature Signature reçue dans les headers
     * @return true si authentique, false sinon
     */
    boolean verifyWebhookSignature(WebhookPaymentNotification notification, String signature);
    
    /**
     * Annule un paiement (si possible)
     * 
     * @param transaction Transaction à annuler
     * @return true si annulé, false sinon
     * @throws SendByOpException Si une erreur se produit
     */
    boolean cancelPayment(Transaction transaction) throws SendByOpException;
    
    /**
     * Initie un remboursement
     * 
     * @param transaction Transaction à rembourser
     * @param reason Raison du remboursement
     * @return true si remboursement initié, false sinon
     * @throws SendByOpException Si une erreur se produit
     */
    boolean refundPayment(Transaction transaction, String reason) throws SendByOpException;
}
