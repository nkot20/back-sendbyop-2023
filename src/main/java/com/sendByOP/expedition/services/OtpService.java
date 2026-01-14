package com.sendByOP.expedition.services;

import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.repositories.UserRepository;
import com.sendByOP.expedition.services.impl.SendMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {
    
    private final UserRepository userRepository;
    private final SendMailService emailService;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 10;
    
    /**
     * Génère un code OTP à 6 chiffres
     */
    public String generateOtpCode() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    /**
     * Envoie un code OTP par email et le stocke en base de données
     */
    @Transactional
    public boolean sendOtpEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            String otpCode = generateOtpCode();
            
            // Stocker le code OTP et la date d'envoi
            user.setOtpSecret(otpCode);
            user.setOtpSentAt(new Date());
            userRepository.save(user);
            
            // Envoyer l'email en HTML
            String subject = "Code de vérification SendByOp";
            String htmlBody = buildOtpEmailBody(user.getFirstName(), otpCode);
            
            emailService.sendHtmlEmail(email, subject, htmlBody);
            
            log.info("OTP envoyé avec succès à {}", email);
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'OTP à {}: {}", email, e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie si le code OTP est valide
     */
    public boolean verifyOtp(String email, String otpCode) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Vérifier si un OTP a été envoyé
            if (user.getOtpSecret() == null || user.getOtpSentAt() == null) {
                log.warn("Aucun OTP trouvé pour {}", email);
                return false;
            }
            
            // Vérifier si le code correspond
            if (!user.getOtpSecret().equals(otpCode)) {
                log.warn("Code OTP incorrect pour {}", email);
                return false;
            }
            
            // Vérifier si le code n'a pas expiré
            LocalDateTime sentAt = user.getOtpSentAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            LocalDateTime expiryTime = sentAt.plusMinutes(OTP_VALIDITY_MINUTES);
            
            if (LocalDateTime.now().isAfter(expiryTime)) {
                log.warn("Code OTP expiré pour {}", email);
                return false;
            }
            
            log.info("Code OTP validé avec succès pour {}", email);
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de l'OTP pour {}: {}", email, e.getMessage());
            return false;
        }
    }
    
    /**
     * Efface le code OTP après utilisation
     */
    @Transactional
    public void clearOtp(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setOtpSecret(null);
            user.setOtpSentAt(null);
            userRepository.save(user);
            
            log.info("OTP effacé pour {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'effacement de l'OTP pour {}: {}", email, e.getMessage());
        }
    }
    
    /**
     * Construit le corps de l'email avec le code OTP
     */
    private String buildOtpEmailBody(String firstName, String otpCode) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #FF6B35, #F9A826); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .otp-code { font-size: 36px; font-weight: bold; color: #FF6B35; text-align: center; letter-spacing: 8px; padding: 30px; background-color: white; border: 2px dashed #FF6B35; border-radius: 10px; margin: 30px 0; }
                        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; font-size: 14px; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Code de vérification</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Voici votre code de vérification pour activer l'authentification à deux facteurs :</p>
                            <div class="otp-code">%s</div>
                            <p style="color: #666; text-align: center;">Code valide pendant <strong>%d minutes</strong></p>
                            <div class="warning">
                                <strong>⚠️ Sécurité :</strong> Ne partagez jamais ce code avec qui que ce soit. 
                                L'équipe SendByOp ne vous demandera jamais votre code de vérification.
                            </div>
                            <p>Si vous n'avez pas demandé ce code, vous pouvez ignorer cet email.</p>
                            <p>Cordialement,<br>L'équipe SendByOp</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 SendByOp. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, firstName, otpCode, OTP_VALIDITY_MINUTES);
    }
}
