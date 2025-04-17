package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.CustomerRegistrationDto;
import com.sendByOP.expedition.models.entities.VerifyToken;
import com.sendByOP.expedition.services.iServices.IClientServivce;
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
public class UserRegistrationService {
    private final IClientServivce customerService;
    private final VerifyTokenService verifyTokenService;
    private final SendMailService emailService;
    private final PasswordEncoder passwordEncoder;

    public CustomerDto registerNewCustomer(CustomerRegistrationDto registrationDto) throws SendByOpException {
        if (customerService.customerIsExist(registrationDto.getEmail())) {
            throw new SendByOpException(ErrorInfo.EMAIL_ALREADY_EXISTS);
        }

        CustomerDto customer = CustomerDto.builder()
            .firstName(registrationDto.getFirstName())
            .lastName(registrationDto.getLastName())
            .email(registrationDto.getEmail())
            .phoneNumber(registrationDto.getPhoneNumber())
            .country(registrationDto.getCountry())
            .address(registrationDto.getAddress())
            .build();

        CustomerDto savedCustomer = customerService.saveClient(customer);
        if (savedCustomer == null) {
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }

        sendVerificationEmail(savedCustomer);
        return savedCustomer;
    }

    private void sendVerificationEmail(CustomerDto customer) throws SendByOpException {
        VerifyToken verifyToken = verifyTokenService.save(customer.getEmail());
        String content = "Hello [[name]],<br>"
                + "We need to verify your email address and phone number before you can access <br>"
                + "<h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>"
                + "Verify your email address"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">Click here</a></h3>"
                + "This link will expire in 24 hours <br>"
                + "Best regards,<br>"
                + "The <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp</a></h3> team<br>"
                + "This is an automated email<br>";

        try {
            emailService.sendVerificationEmail(customer, "http://localhost:4200/verification", 
                verifyToken.getToken(), "/verify?code=", "Account Verification", content);
        } catch (MessagingException e) {
            throw new SendByOpException(ErrorInfo.EMAIL_SEND_ERROR);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void resendVerificationEmail(String email) throws SendByOpException {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        if (customer == null) {
            throw new SendByOpException(ErrorInfo.USER_NOT_FOUND);
        }
        sendVerificationEmail(customer);
    }

    public CustomerDto verifyCustomerEmail(String token) throws SendByOpException {
        String result = verifyTokenService.verifyToken(token);

        if (result.equals(AppConstants.TOKEN_EXPIRED)) {
            throw new SendByOpException(ErrorInfo.TOKEN_EXPIRED);
        }
        if (result.equals(AppConstants.TOKEN_INVALID)) {
            throw new SendByOpException(ErrorInfo.TOKEN_INVALID);
        }
        if (result.equals(AppConstants.TOKEN_VALID)) {
            VerifyToken verifyToken = verifyTokenService.getByTokent(token);
            CustomerDto customer = customerService.getCustomerByEmail(verifyToken.getEmail());
            customer.setEmailVerified(1);
            return customerService.saveClient(customer);
        }
        throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
    }

    public void registerUserAfterPhoneVerification(String phoneNumber) throws SendByOpException {
        CustomerDto customer = customerService.findByNumber(phoneNumber);
        if (customer == null) {
            throw new SendByOpException(ErrorInfo.USER_NOT_FOUND);
        }
        customer.setPhoneVerified(1);
        customerService.saveClient(customer);
    }
}