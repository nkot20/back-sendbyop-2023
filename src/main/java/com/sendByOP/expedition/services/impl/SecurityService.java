package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.repositories.CustomerRepository;
import com.sendByOP.expedition.services.iServices.ISecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService implements ISecurityService {
    
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SendMailService sendMailService;
    
    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRATION_MINUTES = 10;
    
    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) throws SendByOpException {
        log.info("Tentative de changement de mot de passe pour l'utilisateur: {}", email);
        
        // Validation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Le nouveau mot de passe et sa confirmation ne correspondent pas");
        }
        
        // Récupérer le client
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPassword())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Le mot de passe actuel est incorrect");
        }
        
        // Mettre à jour le mot de passe
        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
        
        // Envoyer un email de confirmation
        try {
            String htmlContent = buildPasswordChangeEmailTemplate(
                customer.getFirstName() + " " + customer.getLastName()
            );
            sendMailService.sendHtmlEmail(
                customer.getEmail(),
                "Confirmation de changement de mot de passe",
                htmlContent
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation: {}", e.getMessage());
            // Ne pas faire échouer l'opération si l'email échoue
        }
        
        log.info("Mot de passe changé avec succès pour l'utilisateur: {}", email);
    }
    
    @Override
    @Transactional
    public SecuritySettingsDto toggle2FA(Enable2FARequest request) throws SendByOpException {
        log.info("Tentative de {} 2FA pour l'utilisateur: {}", 
            request.isEnable() ? "activation" : "désactivation", request.getEmail());
        
        Customer customer = customerRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        customer.setTwoFactorEnabled(request.isEnable());
        
        // Si désactivation, nettoyer les données OTP
        if (!request.isEnable()) {
            customer.setOtpSecret(null);
            customer.setOtpSentAt(null);
        }
        
        customerRepository.save(customer);
        
        log.info("2FA {} avec succès pour l'utilisateur: {}", 
            request.isEnable() ? "activé" : "désactivé", request.getEmail());
        
        return buildSecuritySettingsDto(customer);
    }
    
    @Override
    @Transactional
    public void sendOTP(String email) throws SendByOpException {
        log.info("Envoi d'un code OTP à l'utilisateur: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        if (!Boolean.TRUE.equals(customer.getTwoFactorEnabled())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "L'authentification à deux facteurs n'est pas activée pour cet utilisateur");
        }
        
        // Générer un code OTP
        String otpCode = generateOTP();
        
        // Stocker le code OTP et la date d'envoi
        customer.setOtpSecret(passwordEncoder.encode(otpCode));
        customer.setOtpSentAt(new Date());
        customerRepository.save(customer);
        
        // Envoyer le code par email
        try {
            String htmlContent = buildOTPEmailTemplate(
                customer.getFirstName() + " " + customer.getLastName(),
                otpCode
            );
            sendMailService.sendHtmlEmail(
                customer.getEmail(),
                "Votre code de vérification SendByOp",
                htmlContent
            );
            log.info("Code OTP envoyé avec succès à: {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email OTP: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR,
                "Erreur lors de l'envoi du code de vérification");
        }
    }
    
    @Override
    public boolean verifyOTP(Verify2FARequest request) throws SendByOpException {
        log.info("Vérification du code OTP pour l'utilisateur: {}", request.getEmail());
        
        Customer customer = customerRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        if (!Boolean.TRUE.equals(customer.getTwoFactorEnabled())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "L'authentification à deux facteurs n'est pas activée pour cet utilisateur");
        }
        
        if (customer.getOtpSecret() == null || customer.getOtpSentAt() == null) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Aucun code OTP n'a été généré. Veuillez demander un nouveau code.");
        }
        
        // Vérifier l'expiration du code (10 minutes)
        long elapsedMinutes = (new Date().getTime() - customer.getOtpSentAt().getTime()) / (1000 * 60);
        if (elapsedMinutes > OTP_EXPIRATION_MINUTES) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Le code OTP a expiré. Veuillez demander un nouveau code.");
        }
        
        // Vérifier le code
        boolean isValid = passwordEncoder.matches(request.getOtpCode(), customer.getOtpSecret());
        
        if (isValid) {
            log.info("Code OTP vérifié avec succès pour l'utilisateur: {}", request.getEmail());
            // Nettoyer le code OTP après vérification réussie
            customer.setOtpSecret(null);
            customer.setOtpSentAt(null);
            customerRepository.save(customer);
        } else {
            log.warn("Code OTP invalide pour l'utilisateur: {}", request.getEmail());
        }
        
        return isValid;
    }
    
    @Override
    public SecuritySettingsDto getSecuritySettings(String email) throws SendByOpException {
        log.info("Récupération des paramètres de sécurité pour l'utilisateur: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        return buildSecuritySettingsDto(customer);
    }
    
    // Méthodes utilitaires
    
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    private SecuritySettingsDto buildSecuritySettingsDto(Customer customer) {
        return SecuritySettingsDto.builder()
            .email(customer.getEmail())
            .twoFactorEnabled(Boolean.TRUE.equals(customer.getTwoFactorEnabled()))
            .emailVerified(customer.getEmailVerified() == 1)
            .phoneVerified(customer.getPhoneVerified() == 1)
            .build();
    }
    
    private String buildPasswordChangeEmailTemplate(String customerName) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background: linear-gradient(135deg, #0066cc, #0052a3); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
            ".alert { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
            ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>🔒 Mot de passe modifié</h1>" +
            "</div>" +
            "<div class='content'>" +
            "<p>Bonjour <strong>" + customerName + "</strong>,</p>" +
            "<p>Votre mot de passe a été modifié avec succès.</p>" +
            "<div class='alert'>" +
            "<strong>⚠️ Important :</strong> Si vous n'êtes pas à l'origine de cette modification, " +
            "veuillez contacter immédiatement notre support." +
            "</div>" +
            "<p>Date de modification : <strong>" + new Date() + "</strong></p>" +
            "<p>Cordialement,<br>L'équipe SendByOp</p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>© 2026 SendByOp. Tous droits réservés.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
    
    private String buildOTPEmailTemplate(String customerName, String otpCode) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background: linear-gradient(135deg, #0066cc, #0052a3); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
            ".otp-box { background: white; border: 2px dashed #0066cc; border-radius: 10px; padding: 30px; text-align: center; margin: 30px 0; }" +
            ".otp-code { font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #0066cc; }" +
            ".warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
            ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>🔐 Code de vérification</h1>" +
            "</div>" +
            "<div class='content'>" +
            "<p>Bonjour <strong>" + customerName + "</strong>,</p>" +
            "<p>Voici votre code de vérification pour activer l'authentification à deux facteurs :</p>" +
            "<div class='otp-box'>" +
            "<div class='otp-code'>" + otpCode + "</div>" +
            "<p style='color: #666; margin-top: 10px;'>Code valide pendant 10 minutes</p>" +
            "</div>" +
            "<div class='warning'>" +
            "<strong>⚠️ Sécurité :</strong> Ne partagez jamais ce code avec qui que ce soit. " +
            "L'équipe SendByOp ne vous demandera jamais votre code de vérification." +
            "</div>" +
            "<p>Si vous n'avez pas demandé ce code, vous pouvez ignorer cet email.</p>" +
            "<p>Cordialement,<br>L'équipe SendByOp</p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>© 2026 SendByOp. Tous droits réservés.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
}
