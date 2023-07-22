package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.message.LoginForm;
import com.sendByOP.expedition.message.SignUpForm;
import com.sendByOP.expedition.model.Email;
import com.sendByOP.expedition.model.Role;
import com.sendByOP.expedition.model.User;
import com.sendByOP.expedition.reponse.JwtResponse;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.security.jwt.JwtProvider;
import com.sendByOP.expedition.services.servicesImpl.Clientservice;
import com.sendByOP.expedition.services.servicesImpl.RoleService;
import com.sendByOP.expedition.services.servicesImpl.SendMailService;
import com.sendByOP.expedition.services.servicesImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;



@RestController
public class AuthRestAPIsController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    PasswordEncoder encoder;


    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    SendMailService sendMailService;

    @Autowired
    Clientservice clientservice;

    @PostMapping("/api/v1/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {

        System.out.println(loginRequest.getUsername());

        if(loginRequest.getUsername() == null  ) {
            return new ResponseEntity<>(new ResponseMessage("email is null"),
                    HttpStatus.OK);
        }
        //Client client = clientservice.getCustomerByEmail(loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
    }

    @PostMapping("/api/v1/signup/admin")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) throws SendByOpException {
        try {
            if (userService.userIsExist(signUpRequest.getUsername())) {
                return new ResponseEntity<>(new ResponseMessage("Adresse email déja utilisée connectez-vous"),
                        HttpStatus.BAD_REQUEST);
            }

            encoder.encode(signUpRequest.getPw());
            // Creating user's account
            User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getName(), signUpRequest.getLastName(), encoder.encode(signUpRequest.getPw()));
            String strRoles = signUpRequest.getRole();

            Role roles = new Role();

            //RoleName administrateur = RoleName.administrateur;
            roles = roleService.getRoleInfo(strRoles);
            if (roles == null) {
                return new ResponseEntity<>(new ResponseMessage("Aucun droit ne correspond à ce que vous avez entré"), HttpStatus.NOT_FOUND);
            }
            user.setRole(roles);
            userService.saveUser(user);

            return new ResponseEntity<>(new ResponseMessage("User registered successfully!"), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    //change password
    @PostMapping(value = "api/v1/changepw")
    public ResponseEntity<?> changePw(@RequestBody String email, @RequestBody String oldPw, @RequestBody String newPw) throws SendByOpException {
        try {
            User user = userService.findByEmail(email);

            if (!user.getPw().equals(oldPw)){
                return new ResponseEntity<>(new ResponseMessage("Fail -> Mot de passe incorrecte"),
                        HttpStatus.BAD_REQUEST);
            }

            user.setPw(newPw);

            User newUser = userService.saveUser(user);

            if (newUser == null){
                return new ResponseEntity<>(new ResponseMessage("Fail -> Un problème est survenu"),
                        HttpStatus.BAD_REQUEST);
            } else {
                Email sendEmail = new Email();
                sendEmail.setTo(email);
                sendEmail.setBody("Votre mot de passe a été modifié avec succès veuillez!!!");
                sendEmail.setTopic("Modification de mot de passe");
                sendMailService.sendEmail(sendEmail);
            }

            return new ResponseEntity<>(new ResponseMessage("User registered successfully!"), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }

    }

    //Mot de passe oublié.


    @PostMapping(value = "user/delete")
    public ResponseEntity<?> deleteUser(@RequestBody String email) throws SendByOpException {
        try {
            User user = userService.findByEmail(email);
            userService.deleteuser(user);
            return new ResponseEntity<>(new ResponseMessage("User deleted successfully!"), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @PostMapping(value = "user/update")
    public ResponseEntity<?> updateUser(@RequestBody User user) throws SendByOpException {
        try {
            User newUser = userService.updateUser(user);
            if(newUser == null) {
                throw new SendByOpException("Un problème est survenu", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(newUser, HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }


    @GetMapping(value = "getuser/email/{email}")
    public ResponseEntity<?> getUser(@PathVariable("email") String email) throws SendByOpException {
        try {
            User user = userService.findByEmail(email);
            return new ResponseEntity<>(user,
                    HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

}
