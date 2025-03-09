package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.message.LoginForm;
import com.sendByOP.expedition.message.SignUpForm;
import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.Role;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.reponse.JwtResponse;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.security.jwt.JwtProvider;
import com.sendByOP.expedition.services.iServices.IAuthService;
import com.sendByOP.expedition.services.iServices.IClientServivce;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final IUserService userService;
    private final IRoleService roleService;
    private final PasswordEncoder encoder;
    private final JwtProvider jwtProvider;
    private final SendMailService sendMailService;
    private final IClientServivce clientService;

    @Override
    public JwtResponse authenticateUser(@Valid LoginForm loginRequest) throws SendByOpException {
        if (loginRequest.getUsername() == null) {
            throw new SendByOpException("email is null", HttpStatus.OK);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        if (!authentication.isAuthenticated()) {
            throw new SendByOpException("Incorrect email or password", HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateJwtToken(authentication);
        log.info("@@--- JWT Token {}",  jwt);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Check if the user has the "client" role and handle accordingly
        if (userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("client"))) {
            Customer client = clientService.getCustomerByEmail(userDetails.getUsername());

            if (client.getValidEmail() != 1 && client.getValidNumber() != 1) {
                try {
                    sendVerificationEmail(client);
                } catch (MessagingException | UnsupportedEncodingException  e) {
                    throw new SendByOpException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                throw new SendByOpException("Please verify your email and number", HttpStatus.UNAUTHORIZED);
            }
            return new JwtResponse(jwt, userDetails.getUsername(), client, userDetails.getAuthorities());
        }

        return new JwtResponse(jwt, userDetails.getUsername(), null, userDetails.getAuthorities());
    }

    @Override
    public ResponseMessage registerUser(@Valid SignUpForm signUpRequest) throws SendByOpException {
        try {
            if (userService.userIsExist(signUpRequest.getUsername())) {
                throw new SendByOpException("Adresse email déjà utilisée, connectez-vous", HttpStatus.BAD_REQUEST);
            }

            String encodedPassword = encoder.encode(signUpRequest.getPw());
            Role role = roleService.getRoleInfo(signUpRequest.getRole());

            if (role == null) {
                throw new SendByOpException("Aucun droit ne correspond à ce que vous avez entré", HttpStatus.NOT_FOUND);
            }

            User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getName(), signUpRequest.getLastName(), encodedPassword);
            user.setRole(role);
            userService.saveUser(user);

            return new ResponseMessage("User registered successfully!");
        } catch (SendByOpException e) {
            throw e;  // Rethrow exception to be handled by controller
        }
    }

    @Override
    public ResponseMessage changePassword(String email, String oldPw, String newPw) throws SendByOpException {
        try {
            User user = userService.findByEmail(email);
            if (!user.getPw().equals(oldPw)) {
                throw new SendByOpException("Fail -> Mot de passe incorrect", HttpStatus.BAD_REQUEST);
            }

            user.setPw(newPw);
            userService.saveUser(user);

            // Send email notification for password change
            sendPasswordChangeEmail(email);
            return new ResponseMessage("Mot de passe modifié avec succès");
        } catch (SendByOpException e) {
            throw e;  // Rethrow exception to be handled by controller
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

    private void sendVerificationEmail(Customer client) throws MessagingException, UnsupportedEncodingException {
        // Implement sending email logic for client verification
        sendMailService.sendVerificationEmail(client, "http://localhost:4200/verification", "token", "/verify?code=", "Validation de compte", "Email content here");
    }

    private void sendPasswordChangeEmail(String email) {
        EmailDto emailDto = new EmailDto();
        emailDto.setTo(email);
        emailDto.setBody("Votre mot de passe a été modifié avec succès!");
        emailDto.setTopic("Modification de mot de passe");
        sendMailService.sendEmail(emailDto);
    }
}
