package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.VerifyToken;
import com.sendByOP.expedition.repositories.VerifyTokenRepository;
import com.sendByOP.expedition.utils.AppConstants;
import com.sendByOP.expedition.utils.DateUse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VerifyTokenService {

    private final VerifyTokenRepository verifyTokenRepository;

    public VerifyToken save(String email) {
        String token = RandomStringUtils.randomAlphanumeric(64); // ou UUID.randomUUID().toString()

        // Création de l'objet token
        VerifyToken verifyToken = new VerifyToken();
        verifyToken.setEmail(email);
        verifyToken.setToken(token);
        verifyToken.setExpiratedToken(DateUse.calculateExpiryDate(1440)); // 1440 minutes = 24h

        log.info("Generated email verification token for {}: {}", email, token);

        return verifyTokenRepository.save(verifyToken);

    }

    public String verifyToken(String token) {
        VerifyToken verifyToken = verifyTokenRepository.findByToken(token);
        if (verifyToken == null) {
            return AppConstants.TOKEN_INVALID;
        } else{
            if(DateUse.diff(verifyToken.getExpiratedToken(), new Date()) < 0) {
                return AppConstants.TOKEN_EXPIRED;
            } else {
                return AppConstants.TOKEN_VALID;
            }
        }

    }

    public VerifyToken getByTokent(String token) {
        return verifyTokenRepository.findByToken(token);
    }

}
