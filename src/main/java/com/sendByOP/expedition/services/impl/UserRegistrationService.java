package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.CustomerRegistrationDto;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.models.entities.VerifyToken;
import com.sendByOP.expedition.models.enums.AccountStatus;
import com.sendByOP.expedition.models.enums.RoleEnum;
import com.sendByOP.expedition.services.iServices.ICustomerService;
import com.sendByOP.expedition.utils.AppConstants;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserRegistrationService {
    private final ICustomerService customerService;
    private final VerifyTokenService verifyTokenService;
    private final SendMailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Value("${base.url}")
    private String baseUrl;

    public CustomerDto registerNewCustomer(CustomerRegistrationDto registrationDto) throws SendByOpException {
        log.info("user registration {}", registrationDto.getFirstName());
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
        
        // Créer et sauvegarder le compte User
        User savedUser = userService.saveUser(User.builder()
                        .email(customer.getEmail())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .username(customer.getEmail())
                        .password(passwordEncoder.encode(registrationDto.getPassword()))
                        .role(RoleEnum.CUSTOMER.name())
                        .status(AccountStatus.PENDING_VERIFICATION)  // Compte en attente de vérification
                .build());

        if (savedCustomer == null || savedUser == null) {
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Failed to create customer or user account");
        }
        
        log.info("Customer and User account created successfully for email: {}", customer.getEmail());

        sendVerificationEmail(savedCustomer);
        return savedCustomer;
    }

    /**
     * Envoie l'email de vérification de manière SYNCHRONE pour garantir le rollback transactionnel
     * Si l'envoi échoue, toute l'inscription (Customer + User) sera annulée
     */
    private void sendVerificationEmail(CustomerDto customer) throws SendByOpException {
        log.info("Début de l'envoi de l'email de vérification pour: {}", customer.getEmail());
        
        try {
            // Génération du token de vérification
            VerifyToken verifyToken = verifyTokenService.save(customer.getEmail());
            
            // Construction de l'URL de vérification complète
            String verificationUrl = baseUrl + "/verification/verify?code=" + verifyToken.getToken();
            
            // Génération du contenu HTML à partir du template Thymeleaf
            String customerName = customer.getFirstName() + " " + customer.getLastName();
            String htmlContent = emailTemplateService.generateVerificationEmail(customerName, verificationUrl);
            
            // Envoi de l'email - SYNCHRONE pour permettre le rollback si échec
            emailService.sendHtmlEmail(
                customer.getEmail(),
                "Vérification de votre compte SendByOp",
                htmlContent
            );
            
            log.info("Email de vérification envoyé avec succès à: {}", customer.getEmail());
        } catch (MessagingException e) {
            log.error("Erreur d'envoi d'email pour {}: {}", customer.getEmail(), e.getMessage());
            // Cette exception provoquera un rollback de toute la transaction
            throw new SendByOpException(ErrorInfo.EMAIL_SEND_ERROR, "Impossible d'envoyer l'email de vérification");
        } catch (UnsupportedEncodingException e) {
            log.error("Erreur d'encodage pour l'email de {}: {}", customer.getEmail(), e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Erreur d'encodage de l'email");
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'envoi de l'email pour {}: {}", customer.getEmail(), e.getMessage(), e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Erreur lors de l'envoi de l'email de vérification");
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
            
            // Mettre à jour le statut du compte User à ACTIVE
            User user = userService.findByEmail(verifyToken.getEmail());
            user.setStatus(AccountStatus.ACTIVE);
            userService.updateUser(user);
            
            // Marquer l'email comme vérifié dans Customer (pour l'affichage du profil)
            customer.setEmailVerified(1);
            return customerService.updateClient(customer);
        }
        throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
    }

    public void registerUserAfterPhoneVerification(String phoneNumber) throws SendByOpException {
        CustomerDto customer = customerService.findByNumber(phoneNumber);
        if (customer == null) {
            throw new SendByOpException(ErrorInfo.USER_NOT_FOUND);
        }
        customer.setPhoneVerified(1);
        customerService.updateClient(customer);
    }
}