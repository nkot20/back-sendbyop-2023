package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.message.LoginForm;
import com.sendByOP.expedition.message.SignUpForm;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.entities.Role;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.models.enums.AccountStatus;
import com.sendByOP.expedition.models.enums.RoleEnum;
import com.sendByOP.expedition.reponse.JwtResponse;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.security.jwt.JwtProvider;
import com.sendByOP.expedition.services.OtpService;
import com.sendByOP.expedition.services.iServices.IAuthService;
import com.sendByOP.expedition.services.iServices.ICustomerService;
import com.sendByOP.expedition.services.iServices.IRoleService;
import com.sendByOP.expedition.services.iServices.IUserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final IUserService userService;
    private final IRoleService roleService;
    private final PasswordEncoder encoder;
    private final JwtProvider jwtProvider;
    private final SendMailService sendMailService;
    private final ICustomerService clientService;
    private final OtpService otpService;

    @Override
    public JwtResponse authenticateUser(@Valid LoginForm loginRequest) throws SendByOpException {
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            throw new SendByOpException("Email or password cannot be null", HttpStatus.BAD_REQUEST);
        }

        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        // 1. Authentifier avec Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // 2. Récupérer le User et vérifier son statut
        User user = userService.findByEmail(loginRequest.getUsername());
        
        // 3. Vérifier le statut du compte
        if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
            log.warn("Login attempt with unverified email: {}", loginRequest.getUsername());
            // Renvoyer automatiquement l'email de vérification
            CustomerDto customer = clientService.getCustomerByEmail(user.getEmail());
            if (customer != null) {
                try {
                    // Déclencher le renvoi d'email via UserRegistrationService si disponible
                    log.info("Resending verification email to: {}", user.getEmail());
                } catch (Exception e) {
                    log.error("Failed to resend verification email: {}", e.getMessage());
                }
            }
            throw new SendByOpException(ErrorInfo.EMAIL_NOT_VERIFIED);
        }
        
        if (user.getStatus() == AccountStatus.BLOCKED) {
            log.warn("Login attempt with blocked account: {}", loginRequest.getUsername());
            throw new SendByOpException(ErrorInfo.ACCOUNT_BLOCKED);
        }
        
        if (user.getStatus() == AccountStatus.INACTIVE) {
            log.warn("Login attempt with inactive account: {}", loginRequest.getUsername());
            throw new SendByOpException(ErrorInfo.ACCOUNT_INACTIVE);
        }
        
        // 4. Vérifier si le 2FA est activé dans la table user
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            log.info("2FA is enabled for user: {}", loginRequest.getUsername());
            
            // Si aucun code OTP n'est fourni, envoyer un OTP et demander à l'utilisateur de le saisir
            if (loginRequest.getOtpCode() == null || loginRequest.getOtpCode().trim().isEmpty()) {
                log.info("Sending OTP to user: {}", loginRequest.getUsername());
                otpService.sendOtpEmail(user.getEmail());
                
                // Retourner une réponse indiquant qu'un code OTP est requis
                JwtResponse response = new JwtResponse(null, null, user.getEmail(), null);
                response.setRequiresOtp(true);
                response.setMessage("Un code de vérification a été envoyé à votre email");
                return response;
            }
            
            // Si un code OTP est fourni, le vérifier
            log.info("Verifying OTP for user: {}", loginRequest.getUsername());
            if (!otpService.verifyOtp(user.getEmail(), loginRequest.getOtpCode())) {
                log.warn("Invalid or expired OTP for user: {}", loginRequest.getUsername());
                throw new SendByOpException(ErrorInfo.VALIDATION_ERROR, 
                        "Code de vérification invalide ou expiré");
            }
            
            // OTP valide, effacer le code et continuer le login
            otpService.clearOtp(user.getEmail());
            log.info("OTP verified successfully for user: {}", loginRequest.getUsername());
        }
        
        // 5. Statut ACTIVE et (2FA désactivé ou OTP vérifié) - Générer le JWT
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateJwtToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);
        log.info("Login successful for user: {} with status: {}", loginRequest.getUsername(), user.getStatus());
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 6. Récupérer les informations du Customer pour le profil
        Integer customerId = null;
        String profilePictureUrl = null;
        String firstName = null;
        String lastName = null;
        
        // Récupérer le customer pour les informations de profil
        CustomerDto customer = null;
        try {
            customer = clientService.getCustomerByEmail(user.getEmail());
        } catch (SendByOpException e) {
            log.warn("No customer found for user email: {}", user.getEmail());
        }
        
        if (customer != null) {
            customerId = customer.getId();
            firstName = customer.getFirstName();
            lastName = customer.getLastName();
            
            // Construire l'URL de la photo de profil si elle existe
            if (customer.getProfilePicture() != null && !customer.getProfilePicture().trim().isEmpty()) {
                profilePictureUrl = "/api/profile/picture/" + customerId;
            }
            
            log.info("Customer info found for user {}: id={}, profilePicture={}", 
                user.getEmail(), customerId, profilePictureUrl);
        }

        return new JwtResponse(jwt, refreshToken, userDetails.getUsername(), customerId, 
                               profilePictureUrl, firstName, lastName, userDetails.getAuthorities());
    }

    @Override
    public ResponseMessage registerUser(@Valid SignUpForm signUpRequest) throws SendByOpException {
        validateSignUpRequest(signUpRequest);

        try {
            if (userService.userIsExist(signUpRequest.getUsername())) {
                throw new SendByOpException("Email address already in use", HttpStatus.CONFLICT);
            }

            String encodedPassword = encoder.encode(signUpRequest.getPw());
            Role role = roleService.getRoleInfo(signUpRequest.getRole());

            if (role == null) {
                throw new SendByOpException("Invalid role specified", HttpStatus.BAD_REQUEST);
            }
            User user = User.builder()
                .firstName(signUpRequest.getName())
                .email(signUpRequest.getUsername())
                .lastName(signUpRequest.getLastName())
                .username(signUpRequest.getUsername())
                .password(encodedPassword)
                .role(RoleEnum.ADMIN.name())
            .build();
            userService.saveUser(user);
            log.info("New user registered: {}", signUpRequest.getUsername());

            return new ResponseMessage("User registered successfully!");
        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage());
            throw new SendByOpException("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseMessage changePassword(String email, String oldPw, String newPw) throws SendByOpException {
        if (email == null || oldPw == null || newPw == null) {
            throw new SendByOpException("All fields are required", HttpStatus.BAD_REQUEST);
        }

        try {
            User user = userService.findByEmail(email);
            if (user == null) {
                throw new SendByOpException("User not found", HttpStatus.NOT_FOUND);
            }

            if (!encoder.matches(oldPw, user.getPassword())) {
                throw new SendByOpException("Incorrect current password", HttpStatus.UNAUTHORIZED);
            }

            validatePassword(newPw);
            user.setPassword(encoder.encode(newPw));
            userService.saveUser(user);

            sendPasswordChangeEmail(email);
            log.info("Password changed successfully for user: {}", email);
            return new ResponseMessage("Password changed successfully");
        } catch (Exception e) {
            log.error("Error during password change: {}", e.getMessage());
            throw new SendByOpException("Password change failed: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseMessage deleteUser(String email) throws SendByOpException {
        try {
            User user = userService.findByEmail(email);
            userService.deleteuser(user);
            return new ResponseMessage("User deleted successfully!");
        } catch (SendByOpException e) {
            throw e;  // Rethrow exception to be handled by controller
        }
    }

    @Override
    public User updateUser(User user) throws SendByOpException {
        try {
            User updatedUser = userService.updateUser(user);
            if (updatedUser == null) {
                throw new SendByOpException("Un problème est survenu", HttpStatus.NOT_FOUND);
            }
            return updatedUser;
        } catch (SendByOpException e) {
            throw e;  // Rethrow exception to be handled by controller
        }
    }

    @Override
    public User getUserByEmail(String email) throws SendByOpException {
        try {
            return userService.findByEmail(email);
        } catch (SendByOpException e) {
            throw e;  // Rethrow exception to be handled by controller
        }
    }

    private void validateSignUpRequest(SignUpForm signUpRequest) throws SendByOpException {
        if (signUpRequest.getUsername() == null ||
            signUpRequest.getPw() == null || signUpRequest.getName() == null || 
            signUpRequest.getLastName() == null) {
            throw new SendByOpException("All fields are required", HttpStatus.BAD_REQUEST);
        }
        validatePassword(signUpRequest.getPw());
    }

    private void validatePassword(String password) throws SendByOpException {
        if (password.length() < 8) {
            throw new SendByOpException("Password must be at least 8 characters long", 
                                      HttpStatus.BAD_REQUEST);
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new SendByOpException("Password must contain at least one uppercase letter", 
                                      HttpStatus.BAD_REQUEST);
        }
        if (!password.matches(".*[a-z].*")) {
            throw new SendByOpException("Password must contain at least one lowercase letter", 
                                      HttpStatus.BAD_REQUEST);
        }
        if (!password.matches(".*\\d.*")) {
            throw new SendByOpException("Password must contain at least one number", 
                                      HttpStatus.BAD_REQUEST);
        }
    }

    private void sendVerificationEmail(CustomerDto client) throws MessagingException, UnsupportedEncodingException, SendByOpException {
        try {
            String verificationUrl = "http://localhost:4200/verification";
            sendMailService.sendVerificationEmail(client, verificationUrl, "token", 
                                                "/verify?code=", "Account Verification", 
                                                "Please verify your account");
            log.info("Verification email sent to: {}", client.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            throw new SendByOpException("Failed to send verification email", 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendPasswordChangeEmail(String email) {
        EmailDto emailDto = EmailDto.builder()
                .to(email)
                .body("Votre mot de passe a été modifié avec succès!")
                .topic("Modification de mot de passe")
                .build();
        sendMailService.sendEmail(emailDto);
    }
}
