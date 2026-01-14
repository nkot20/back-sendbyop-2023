package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.VerifyToken;
import com.sendByOP.expedition.repositories.VerifyTokenRepository;
import com.sendByOP.expedition.utils.AppConstants;
import com.sendByOP.expedition.utils.DateUse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VerifyTokenService {

    private final VerifyTokenRepository verifyTokenRepository;

    public VerifyToken save(String email) {
        // Vérifier si un token existe déjà pour cet email
        Optional<VerifyToken> existingToken = verifyTokenRepository.findByEmail(email);
        if (existingToken.isPresent()) {
            log.info("Token existant trouvé pour {}, suppression de l'ancien token", email);
            verifyTokenRepository.delete(existingToken.get());
        }

        String token = RandomStringUtils.randomAlphanumeric(64);

        // Création de l'objet token
        VerifyToken verifyToken = new VerifyToken();
        verifyToken.setEmail(email);
        verifyToken.setToken(token);
        verifyToken.setExpiratedToken(DateUse.calculateExpiryDate(1440)); // 1440 minutes = 24h

        log.info("Token de vérification email généré pour {}", email);

        return verifyTokenRepository.save(verifyToken);
    }

    public String verifyToken(String token) {
        VerifyToken verifyToken = verifyTokenRepository.findByToken(token);
        if (verifyToken == null) {
            log.warn("Token invalide reçu: {}", token);
            return AppConstants.TOKEN_INVALID;
        } else {
            if(DateUse.diff(verifyToken.getExpiratedToken(), new Date()) < 0) {
                log.warn("Token expiré pour l'email: {}", verifyToken.getEmail());
                return AppConstants.TOKEN_EXPIRED;
            } else {
                log.info("Token valide vérifié pour l'email: {}", verifyToken.getEmail());
                return AppConstants.TOKEN_VALID;
            }
        }
    }

    public VerifyToken getByTokent(String token) {
        return verifyTokenRepository.findByToken(token);
    }

    /**
     * Nettoie automatiquement les tokens expirés tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        log.info("Démarrage du nettoyage des tokens expirés");
        try {
            Date now = new Date();
            long expiredCount = verifyTokenRepository.countExpiredTokens(now);
            
            if (expiredCount > 0) {
                verifyTokenRepository.deleteExpiredTokens(now);
                log.info("Nettoyage terminé: {} tokens expirés supprimés", expiredCount);
            } else {
                log.info("Aucun token expiré à supprimer");
            }
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des tokens expirés: {}", e.getMessage(), e);
        }
    }
}
