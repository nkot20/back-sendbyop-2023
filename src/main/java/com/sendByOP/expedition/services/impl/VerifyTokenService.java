package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.VerifyToken;
import com.sendByOP.expedition.repositories.VerifyTokenRepository;
import com.sendByOP.expedition.utils.AppConstants;
import com.sendByOP.expedition.utils.DateUse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class VerifyTokenService {

    private final VerifyTokenRepository verifyTokenRepository;

    public VerifyToken save(String email) {
        VerifyToken verifyToken = new VerifyToken();
        DateUse dateUse = new DateUse();

        verifyToken.setEmail(email);
        verifyToken.setExpiratedToken(DateUse.calculateExpiryDate(1440));
        verifyToken.setToken(RandomStringUtils.random(64));

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
