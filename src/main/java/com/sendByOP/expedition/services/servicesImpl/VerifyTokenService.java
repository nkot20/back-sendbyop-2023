package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.model.VerifyToken;
import com.sendByOP.expedition.repositories.VerifyTokenRepository;
import com.sendByOP.expedition.utils.AppConstants;
import com.sendByOP.expedition.utils.DateUse;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

@Service
@Transactional
public class VerifyTokenService {

    @Autowired
    VerifyTokenRepository verifyTokenRepository;

    @Autowired
    Clientservice clientservice;

    public VerifyToken save(String email) {
        VerifyToken verifyToken = new VerifyToken();
        DateUse dateUse = new DateUse();

        verifyToken.setEmail(email);
        verifyToken.setExpiratedToken(DateUse.calculateExpiryDate(1440));
        verifyToken.setToken(RandomString.make(64));

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
