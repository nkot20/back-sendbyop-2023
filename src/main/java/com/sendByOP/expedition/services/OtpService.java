package com.sendByOP.expedition.services;

import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.repositories.CustomerRepository;
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
    
    private final CustomerRepository customerRepository;
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
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));
            
            String otpCode = generateOtpCode();
            
            // Stocker le code OTP et la date d'envoi
            customer.setOtpSecret(otpCode);
            customer.setOtpSentAt(new Date());
            customerRepository.save(customer);
            
            // Envoyer l'email
            String subject = "Code de vérification SendByOp";
            String body = buildOtpEmailBody(customer.getFirstName(), otpCode);
            
            EmailDto emailDto = EmailDto.builder()
                    .to(email)
                    .topic(subject)
                    .body(body)
                    .build();
            emailService.sendEmail(emailDto);
            
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
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));
            
            // Vérifier si un OTP a été envoyé
            if (customer.getOtpSecret() == null || customer.getOtpSentAt() == null) {
                log.warn("Aucun OTP trouvé pour {}", email);
                return false;
            }
            
            // Vérifier si le code correspond
            if (!customer.getOtpSecret().equals(otpCode)) {
                log.warn("Code OTP incorrect pour {}", email);
                return false;
            }
            
            // Vérifier si le code n'a pas expiré
            LocalDateTime sentAt = customer.getOtpSentAt().toInstant()
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
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));
            
            customer.setOtpSecret(null);
            customer.setOtpSentAt(null);
            customerRepository.save(customer);
            
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
                        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; }
                        .otp-code { font-size: 32px; font-weight: bold; color: #007bff; text-align: center; letter-spacing: 5px; padding: 20px; background-color: white; border: 2px dashed #007bff; border-radius: 5px; margin: 20px 0; }
                        .warning { color: #dc3545; font-size: 14px; margin-top: 20px; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>SendByOp - Code de vérification</h2>
                        </div>
                        <div class="content">
                            <p>Bonjour %s,</p>
                            <p>Vous avez demandé un code de vérification pour accéder à votre compte SendByOp.</p>
                            <p>Voici votre code de vérification :</p>
                            <div class="otp-code">%s</div>
                            <p>Ce code est valide pendant <strong>%d minutes</strong>.</p>
                            <div class="warning">
                                ⚠️ Si vous n'avez pas demandé ce code, veuillez ignorer cet email et contacter notre support.
                            </div>
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
