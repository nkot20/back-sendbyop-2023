package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;

public interface ISecurityService {
    
    /**
     * Change le mot de passe d'un utilisateur
     * @param email Email de l'utilisateur
     * @param request Requête de changement de mot de passe
     * @throws SendByOpException Si une erreur se produit
     */
    void changePassword(String email, ChangePasswordRequest request) throws SendByOpException;
    
    /**
     * Active ou désactive l'authentification à deux facteurs
     * @param request Requête d'activation/désactivation
     * @return Les paramètres de sécurité mis à jour
     * @throws SendByOpException Si une erreur se produit
     */
    SecuritySettingsDto toggle2FA(Enable2FARequest request) throws SendByOpException;
    
    /**
     * Envoie un code OTP par email
     * @param email Email de l'utilisateur
     * @throws SendByOpException Si une erreur se produit
     */
    void sendOTP(String email) throws SendByOpException;
    
    /**
     * Vérifie un code OTP
     * @param request Requête de vérification
     * @return true si le code est valide, false sinon
     * @throws SendByOpException Si une erreur se produit
     */
    boolean verifyOTP(Verify2FARequest request) throws SendByOpException;
    
    /**
     * Récupère les paramètres de sécurité d'un utilisateur
     * @param email Email de l'utilisateur
     * @return Les paramètres de sécurité
     * @throws SendByOpException Si une erreur se produit
     */
    SecuritySettingsDto getSecuritySettings(String email) throws SendByOpException;
}
