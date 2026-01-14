package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.repositories.UserRepository;
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
    
    private final UserRepository userRepository;
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
        
        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Le mot de passe actuel est incorrect");
        }
        
        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Envoyer un email de confirmation
        try {
            String htmlContent = buildPasswordChangeEmailTemplate(
                user.getFirstName() + " " + user.getLastName()
            );
            sendMailService.sendHtmlEmail(
                user.getEmail(),
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
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        user.setTwoFactorEnabled(request.isEnable());
        
        // Si désactivation, nettoyer les données OTP
        if (!request.isEnable()) {
            user.setOtpSecret(null);
            user.setOtpSentAt(null);
        }
        
        userRepository.save(user);
        
        log.info("2FA {} avec succès pour l'utilisateur: {}", 
            request.isEnable() ? "activé" : "désactivé", request.getEmail());
        
        return buildSecuritySettingsDto(user);
    }
    
    @Override
    @Transactional
    public void sendOTP(String email) throws SendByOpException {
        log.info("Envoi d'un code OTP à l'utilisateur: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "L'authentification à deux facteurs n'est pas activée pour cet utilisateur");
        }
        
        // Générer un code OTP
        String otpCode = generateOTP();
        
        // Stocker le code OTP et la date d'envoi
        user.setOtpSecret(passwordEncoder.encode(otpCode));
        user.setOtpSentAt(new Date());
        userRepository.save(user);
        
        // Envoyer le code par email
        try {
            String htmlContent = buildOTPEmailTemplate(
                user.getFirstName() + " " + user.getLastName(),
                otpCode
            );
            sendMailService.sendHtmlEmail(
                user.getEmail(),
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
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "L'authentification à deux facteurs n'est pas activée pour cet utilisateur");
        }
        
        if (user.getOtpSecret() == null || user.getOtpSentAt() == null) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Aucun code OTP n'a été généré. Veuillez demander un nouveau code.");
        }
        
        // Vérifier l'expiration du code (10 minutes)
        long elapsedMinutes = (new Date().getTime() - user.getOtpSentAt().getTime()) / (1000 * 60);
        if (elapsedMinutes > OTP_EXPIRATION_MINUTES) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Le code OTP a expiré. Veuillez demander un nouveau code.");
        }
        
        // Vérifier le code
        boolean isValid = passwordEncoder.matches(request.getOtpCode(), user.getOtpSecret());
        
        if (isValid) {
            log.info("Code OTP vérifié avec succès pour l'utilisateur: {}", request.getEmail());
            // Nettoyer le code OTP après vérification réussie
            user.setOtpSecret(null);
            user.setOtpSentAt(null);
            userRepository.save(user);
        } else {
            log.warn("Code OTP invalide pour l'utilisateur: {}", request.getEmail());
        }
        
        return isValid;
    }
    
    @Override
    public SecuritySettingsDto getSecuritySettings(String email) throws SendByOpException {
        log.info("Récupération des paramètres de sécurité pour l'utilisateur: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                "Utilisateur non trouvé"));
        
        return buildSecuritySettingsDto(user);
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
    
    private SecuritySettingsDto buildSecuritySettingsDto(User user) {
        return SecuritySettingsDto.builder()
            .email(user.getEmail())
            .twoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()))
            .emailVerified(true) // Les utilisateurs dans la table user sont considérés comme vérifiés
            .phoneVerified(false) // La table user n'a pas de vérification de téléphone
            .build();
    }
    
    private String buildPasswordChangeEmailTemplate(String customerName) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background: linear-gradient(135deg, #FF6B35, #F9A826); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
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
            ".header { background: linear-gradient(135deg, #FF6B35, #F9A826); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
            ".otp-box { background: white; border: 2px dashed #FF6B35; border-radius: 10px; padding: 30px; text-align: center; margin: 30px 0; }" +
            ".otp-code { font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #FF6B35; }" +
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
    
    /**
     * Demande de réinitialisation de mot de passe
     * Génère et envoie un OTP par email
     */
    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Demande de réinitialisation de mot de passe pour: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new SendByOpException(ErrorInfo.USER_NOT_FOUND,
                        "Aucun compte trouvé avec cet email"));
        
        // Générer un OTP
        String otp = generateOTP();
        
        // Hasher et sauvegarder l'OTP
        user.setOtpSecret(passwordEncoder.encode(otp));
        user.setOtpSentAt(new Date());
        userRepository.save(user);
        
        // Envoyer l'email avec l'OTP
        try {
            sendMailService.sendHtmlEmail(
                    user.getEmail(),
                    "Réinitialisation de votre mot de passe SendByOp",
                    buildPasswordResetEmail(user.getFirstName() + " " + user.getLastName(), otp)
            );
            
            log.info("Email de réinitialisation envoyé à: {}", user.getEmail());
            
            return ApiResponse.builder()
                    .success(true)
                    .message("Un code de vérification a été envoyé à votre adresse email")
                    .build();
                    
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation", e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR,
                    "Erreur lors de l'envoi de l'email. Veuillez réessayer.");
        }
    }
    
    /**
     * Réinitialisation du mot de passe avec OTP
     */
    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        log.info("Tentative de réinitialisation de mot de passe pour: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new SendByOpException(ErrorInfo.USER_NOT_FOUND,
                        "Aucun compte trouvé avec cet email"));
        
        // Vérifier si un OTP a été envoyé
        if (user.getOtpSecret() == null || user.getOtpSentAt() == null) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Aucune demande de réinitialisation en cours. Veuillez d'abord demander un code.");
        }
        
        // Vérifier l'expiration de l'OTP
        long minutesElapsed = (new Date().getTime() - user.getOtpSentAt().getTime()) / (1000 * 60);
        if (minutesElapsed > OTP_EXPIRATION_MINUTES) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Le code de vérification a expiré. Veuillez demander un nouveau code.");
        }
        
        // Vérifier l'OTP
        if (!passwordEncoder.matches(request.getOtpCode(), user.getOtpSecret())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Code de vérification invalide");
        }
        
        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // Nettoyer l'OTP
        user.setOtpSecret(null);
        user.setOtpSentAt(null);
        
        userRepository.save(user);
        
        // Envoyer un email de confirmation
        try {
            sendMailService.sendHtmlEmail(
                    user.getEmail(),
                    "Mot de passe réinitialisé avec succès",
                    buildPasswordChangedConfirmationEmail(user.getFirstName() + " " + user.getLastName())
            );
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'email de confirmation de changement de mot de passe", e);
            // On ne fait pas échouer la réinitialisation si l'email de confirmation ne peut pas être envoyé
        }
        
        log.info("Mot de passe réinitialisé avec succès pour: {}", user.getEmail());
        
        return ApiResponse.builder()
                .success(true)
                .message("Votre mot de passe a été réinitialisé avec succès")
                .build();
    }
    
    /**
     * Template d'email pour la réinitialisation du mot de passe
     */
    private String buildPasswordResetEmail(String customerName, String otp) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background: linear-gradient(135deg, #FF6B35 0%, #F9A826 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            ".content { background: #f9f9f9; padding: 30px; }" +
            ".otp-box { background: white; border: 2px dashed #FF6B35; padding: 20px; margin: 20px 0; text-align: center; border-radius: 8px; }" +
            ".otp-code { font-size: 32px; font-weight: bold; color: #FF6B35; letter-spacing: 8px; }" +
            ".warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
            ".footer { background: #333; color: white; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>🔒 Réinitialisation de mot de passe</h1>" +
            "</div>" +
            "<div class='content'>" +
            "<p>Bonjour <strong>" + customerName + "</strong>,</p>" +
            "<p>Vous avez demandé la réinitialisation de votre mot de passe SendByOp.</p>" +
            "<p>Voici votre code de vérification :</p>" +
            "<div class='otp-box'>" +
            "<div class='otp-code'>" + otp + "</div>" +
            "</div>" +
            "<div class='warning'>" +
            "<strong>⚠️ Important :</strong>" +
            "<ul>" +
            "<li>Ce code est valable pendant <strong>10 minutes</strong></li>" +
            "<li>Ne partagez jamais ce code avec personne</li>" +
            "<li>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email</li>" +
            "</ul>" +
            "</div>" +
            "<p>Cordialement,<br>L'équipe SendByOp</p>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>© 2026 SendByOp. Tous droits réservés.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
    
    /**
     * Template d'email de confirmation de changement de mot de passe
     */
    private String buildPasswordChangedConfirmationEmail(String customerName) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background: linear-gradient(135deg, #FF6B35 0%, #F9A826 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
            ".content { background: #f9f9f9; padding: 30px; }" +
            ".success-box { background: #ffe8dc; border-left: 4px solid #FF6B35; padding: 15px; margin: 20px 0; }" +
            ".footer { background: #333; color: white; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h1>✅ Mot de passe modifié</h1>" +
            "</div>" +
            "<div class='content'>" +
            "<p>Bonjour <strong>" + customerName + "</strong>,</p>" +
            "<div class='success-box'>" +
            "<strong>✓ Votre mot de passe a été modifié avec succès</strong>" +
            "</div>" +
            "<p>Si vous n'êtes pas à l'origine de ce changement, contactez-nous immédiatement.</p>" +
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
