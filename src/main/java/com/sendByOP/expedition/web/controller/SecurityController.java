package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.services.impl.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/security")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SecurityController {
    
    private final SecurityService securityService;
    
    /**
     * Endpoint pour changer le mot de passe
     */
    @PutMapping("/change-password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {
        
        log.info("Requête de changement de mot de passe pour {}", principal.getName());
        
        try {
            securityService.changePassword(principal.getName(), request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mot de passe changé avec succès");
            return ResponseEntity.ok(response);
        } catch (SendByOpException e) {
            log.error("Erreur lors du changement de mot de passe: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint pour récupérer le statut du 2FA
     */
    @GetMapping("/two-factor/status")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SecuritySettingsDto> getTwoFactorStatus(Principal principal) {
        log.info("Récupération du statut 2FA pour {}", principal.getName());
        
        try {
            SecuritySettingsDto settings = securityService.getSecuritySettings(principal.getName());
            return ResponseEntity.ok(settings);
        } catch (SendByOpException e) {
            log.error("Erreur lors de la récupération du statut 2FA: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Endpoint pour activer/désactiver le 2FA
     */
    @PostMapping("/two-factor/toggle")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SecuritySettingsDto> toggleTwoFactor(
            @Valid @RequestBody Enable2FARequest request,
            Principal principal) {
        
        log.info("Requête de {} 2FA pour {}", 
                request.isEnable() ? "activation" : "désactivation", principal.getName());
        
        // S'assurer que l'email correspond à l'utilisateur connecté
        request.setEmail(principal.getName());
        
        try {
            SecuritySettingsDto settings = securityService.toggle2FA(request);
            return ResponseEntity.ok(settings);
        } catch (SendByOpException e) {
            log.error("Erreur lors de la modification du 2FA: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Endpoint pour envoyer un code OTP
     */
    @PostMapping("/two-factor/send-otp")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> sendOtp(Principal principal) {
        log.info("Envoi d'un code OTP pour {}", principal.getName());
        
        try {
            securityService.sendOTP(principal.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Un code de vérification a été envoyé à votre email");
            response.put("otpSent", true);
            return ResponseEntity.ok(response);
        } catch (SendByOpException e) {
            log.error("Erreur lors de l'envoi de l'OTP: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint pour confirmer l'activation du 2FA avec le code OTP
     */
    @PostMapping("/two-factor/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> confirmTwoFactorActivation(
            @Valid @RequestBody Verify2FARequest request,
            Principal principal) {
        
        log.info("Confirmation de l'activation 2FA pour {}", principal.getName());
        
        // S'assurer que l'email correspond à l'utilisateur connecté
        request.setEmail(principal.getName());
        
        try {
            boolean isValid = securityService.verifyOTP(request);
            
            Map<String, Object> response = new HashMap<>();
            if (isValid) {
                response.put("success", true);
                response.put("message", "Authentification à deux facteurs activée avec succès");
                response.put("twoFactorEnabled", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Code de vérification invalide");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (SendByOpException e) {
            log.error("Erreur lors de la vérification du code OTP: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint pour renvoyer un code OTP (si expiré)
     */
    @PostMapping("/two-factor/resend-otp")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> resendOtp(Principal principal) {
        log.info("Renvoi de l'OTP pour {}", principal.getName());
        
        try {
            securityService.sendOTP(principal.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Code de vérification renvoyé");
            return ResponseEntity.ok(response);
        } catch (SendByOpException e) {
            log.error("Erreur lors du renvoi de l'OTP: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
