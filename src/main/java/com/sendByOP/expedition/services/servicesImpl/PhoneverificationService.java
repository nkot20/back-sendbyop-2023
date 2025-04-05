package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.Twilio.Twilioproperties;
import com.sendByOP.expedition.exception.PhoneVerificationException;
import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PhoneverificationService {
    private final Twilioproperties twilioproperties;

    public VerificationResult startVerification(String phone) {
        log.info("Starting phone verification for number: {}", phone);
        validatePhoneNumber(phone);

        try {
            Verification verification = Verification.creator(twilioproperties.getServiceId(), phone, "sms").create();
            String status = verification.getStatus();
            
            if ("approved".equals(status) || "pending".equals(status)) {
                log.info("Verification started successfully for phone: {}, status: {}", phone, status);
                return new VerificationResult(verification.getSid());
            }
            
            log.error("Verification failed for phone: {}, status: {}", phone, status);
            throw new PhoneVerificationException("Verification failed", PhoneVerificationException.ErrorCodes.VERIFICATION_FAILED);
        } catch (ApiException exception) {
            log.error("Twilio API error during verification start: {}", exception.getMessage());
            throw new PhoneVerificationException("Service unavailable", PhoneVerificationException.ErrorCodes.SERVICE_UNAVAILABLE);
        }
    }

    public VerificationResult checkVerification(String phone, String code) {
        log.info("Checking verification code for phone: {}", phone);
        validatePhoneNumber(phone);
        validateVerificationCode(code);

        try {
            VerificationCheck verification = VerificationCheck.creator(twilioproperties.getServiceId(), code)
                    .setTo(phone)
                    .create();

            if ("approved".equals(verification.getStatus())) {
                log.info("Verification successful for phone: {}", phone);
                return new VerificationResult(verification.getSid());
            }

            log.warn("Invalid verification code for phone: {}", phone);
            throw new PhoneVerificationException("Invalid verification code", PhoneVerificationException.ErrorCodes.INVALID_CODE);
        } catch (ApiException exception) {
            log.error("Twilio API error during code verification: {}", exception.getMessage());
            throw new PhoneVerificationException("Service unavailable", PhoneVerificationException.ErrorCodes.SERVICE_UNAVAILABLE);
        }
    }

    private void validatePhoneNumber(String phone) {
        if (!StringUtils.hasText(phone) || !phone.matches("^\\+?[1-9]\\d{1,14}$")) {
            log.error("Invalid phone number format: {}", phone);
            throw new PhoneVerificationException("Invalid phone number format", PhoneVerificationException.ErrorCodes.INVALID_PHONE_NUMBER);
        }
    }

    private void validateVerificationCode(String code) {
        if (!StringUtils.hasText(code) || !code.matches("^\\d{4,10}$")) {
            log.error("Invalid verification code format");
            throw new PhoneVerificationException("Invalid verification code format", PhoneVerificationException.ErrorCodes.INVALID_CODE);
        }
    }

}