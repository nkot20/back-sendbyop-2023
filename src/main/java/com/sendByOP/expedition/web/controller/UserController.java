package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.SendMailService;
import com.sendByOP.expedition.services.servicesImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    SendMailService sendMailService;

    @Autowired
    PasswordEncoder encoder;




    @GetMapping(value = "/list/user")
    public ResponseEntity<?> getAllUser() {
        List<User> users = userService.getAllUser();

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PutMapping(value = "/user/update/pw/{email}")
    public ResponseEntity<?> updatePw(@RequestBody String pw, @PathVariable("email") String email) throws SendByOpException {
        try {
            User user = userService.findByEmail(email);

            if(encoder.matches(pw, user.getPw())) return new ResponseEntity<>(new ResponseMessage("Mot de passe incorrect"), HttpStatus.BAD_REQUEST);

            user.setPw(encoder.encode(pw));


            User newUser =userService.updateUser(user);

            if(newUser == null) return new ResponseEntity<>(new ResponseMessage("Utilisateur introuvable"), HttpStatus.INTERNAL_SERVER_ERROR);

            EmailDto sendEmail = new EmailDto();
            sendEmail.setTo(email);
            sendEmail.setBody("Votre mot de passe a été modifié avec succès veuillez!!!");
            sendEmail.setTopic("Modification de mot de passe");
            sendMailService.sendEmail(sendEmail);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());

        }
    }



}
