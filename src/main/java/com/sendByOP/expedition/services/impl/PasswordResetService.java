package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.models.entities.VerifyToken;
import com.sendByOP.expedition.services.iServices.ICustomerService;
import com.sendByOP.expedition.utils.AppConstants;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {
    private final ICustomerService customerService;
    private final VerifyTokenService verifyTokenService;
    private final SendMailService emailService;
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;

    public VerifyToken initiatePasswordReset(String email) throws SendByOpException {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        if (customer == null) {
            throw new SendByOpException(ErrorInfo.USER_NOT_FOUND);
        }

        VerifyToken verifyToken = verifyTokenService.save(email);
        String content = "Hello [[name]],<br>"
                + "Click here to reset your password"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">Reset Password</a></h3>"
                + "This link will expire in 24 hours <br>"
                + "Best regards,<br>"
                + "The <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp</a></h3> team<br>"
                + "This is an automated email<br>";

        try {
            emailService.sendVerificationEmail(customer, "http://localhost:4200/reset-password", 
                verifyToken.getToken(), "/reset?code=", "Reset Password", content);
        } catch (MessagingException e) {
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Sending email failed");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return verifyToken;
    }

    public String verifyResetToken(String token) throws SendByOpException {
        String result = verifyTokenService.verifyToken(token);

        if (result.equals(AppConstants.TOKEN_EXPIRED)) {
            throw new SendByOpException(ErrorInfo.TOKEN_EXPIRED);
        }
        if (result.equals(AppConstants.TOKEN_INVALID)) {
            throw new SendByOpException(ErrorInfo.TOKEN_INVALID);
        }
        if (result.equals(AppConstants.TOKEN_VALID)) {
            return AppConstants.TOKEN_VALID;
        }
        throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
    }

    public void resetPassword(String token, String newPassword) throws SendByOpException {
        verifyResetToken(token);
        VerifyToken verifyToken = verifyTokenService.getByTokent(token);
        User user = userService.findByEmail(verifyToken.getEmail());
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user);
    }
}