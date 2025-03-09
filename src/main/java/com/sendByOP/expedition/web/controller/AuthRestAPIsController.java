package com.sendByOP.expedition.web.controller;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.message.LoginForm;
import com.sendByOP.expedition.message.SignUpForm;
import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.reponse.JwtResponse;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.reponse.ResponseMessages;
import com.sendByOP.expedition.services.iServices.IAuthService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestAPIsController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginForm loginRequest) throws SendByOpException {
        log.info("###@@ ---------- LOGIN---------------- {}", loginRequest.toString());
        JwtResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/admin")
    public ResponseEntity<ResponseMessage> registerUser(@Valid @RequestBody SignUpForm signUpRequest) throws SendByOpException {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(new ResponseMessage(ResponseMessages.USER_REGISTERED_SUCCESSFULLY.getMessage()));
    }

    @PostMapping("/changepw")
    public ResponseEntity<ResponseMessage> changePassword(@RequestBody EmailDto emailDto) throws SendByOpException {
        authService.changePassword(emailDto.getTo(), emailDto.getBody(), emailDto.getTopic());
        return ResponseEntity.ok(new ResponseMessage(ResponseMessages.PASSWORD_UPDATED_SUCCESSFULLY.getMessage()));
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseMessage> deleteUser(@RequestBody String email) throws SendByOpException {
        authService.deleteUser(email);
        return ResponseEntity.ok(new ResponseMessage(ResponseMessages.USER_DELETED_SUCCESSFULLY.getMessage()));
    }

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody User user) throws SendByOpException {
        User updatedUser = authService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) throws SendByOpException {
        User user = authService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
}
