package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Role;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TwilioController {

    @Autowired
    PhoneverificationService phonesmsservice;

    @Autowired
    Clientservice clientservice;

    @Autowired
    RoleService roleService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserService userService;
    /*@RequestMapping("/")
    public String homepage(ModelAndView model)
    {
        return "index";
    }*/

    @PostMapping("api/v1/sendotp")
    public ResponseEntity<?> sendotp(@RequestBody String phone)
    {
        VerificationResult result=phonesmsservice.startVerification(phone);
        if(result.isValid())
        {
            return new ResponseEntity<>("Code Sent..",HttpStatus.OK);
        }
        return new ResponseEntity<>("Code failed to sent..",HttpStatus.BAD_REQUEST);
    }

    /*@PostMapping("api/v1/verifyotp/{phone}/{id}")
    public ResponseEntity<String> sendotp(@PathVariable("phone") String phone, @RequestBody String otp, @PathVariable("id") int id)
    {
        //System.out.println(otp);

        VerificationResult result=phonesmsservice.checkverification(phone,otp);
        if(result.isValid())
        {
            Client client = clientservice.getClientById(id);

            client.setValidNumber(1);

            Client newClient = clientservice.updateClient(client);

            if (client == null) return new ResponseEntity<>("Un problème est survenu",HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>("Your number is Verified",HttpStatus.OK);
        }
        return new ResponseEntity<>("Something wrong/ Otp incorrect",HttpStatus.BAD_REQUEST);
    }*/

    @PostMapping("api/v1/verifyotp/{phone}/")
    public ResponseEntity<?> sendotp(@PathVariable("phone") String phone, @RequestBody String otp) throws SendByOpException {
        //System.out.println(otp);

        VerificationResult result=phonesmsservice.checkverification(phone,otp);
        if(result.isValid())
        {

            try {
                Client client = clientservice.findByNumber(phone);
                User user = new User(client.getEmail(), client.getEmail(), client.getNom(), client.getPrenom(), encoder.encode(client.getPw()));

                Role role = new Role();

                role = roleService.findByIntitule("client");

                user.setRole(role);
                if (client == null) {
                    throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
                } else {

                    User newUser = userService.saveUser(user);

                    if (newUser == null) {
                        throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
                    }

                }
                return new ResponseEntity<>(new ResponseMessage("Your number is Verified"),HttpStatus.OK);
            } catch (SendByOpException e) {
                return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
            }

        }
        return new ResponseEntity<>(new ResponseMessage("Incorrect code"),HttpStatus.BAD_REQUEST);
    }


}
