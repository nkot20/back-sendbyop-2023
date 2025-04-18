package com.sendByOP.expedition.web.controller;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.message.LoginForm;
import com.sendByOP.expedition.message.SignUpForm;
import com.sendByOP.expedition.models.dto.CustomerRegistrationDto;
import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.reponse.JwtResponse;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.reponse.ResponseMessages;
import com.sendByOP.expedition.services.iServices.IAuthService;
import com.sendByOP.expedition.services.impl.UserRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and management")
public class AuthRestAPIsController {

    private final IAuthService authService;
    private final UserRegistrationService userRegistrationService;

    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginForm loginRequest) throws SendByOpException {
        log.info("###@@ ---------- LOGIN---------------- {}", loginRequest.toString());
        JwtResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register new admin user", description = "Creates a new admin user account")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    @PostMapping("/signup/admin")
    @Profile("dev")
    public ResponseEntity<ResponseMessage> registerUser(@Valid @RequestBody SignUpForm signUpRequest) throws SendByOpException {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(new ResponseMessage(ResponseMessages.USER_REGISTERED_SUCCESSFULLY.getMessage()));
    }

    @Operation(summary = "Change password", description = "Initiates password change process")
    @ApiResponse(responseCode = "200", description = "Password change initiated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
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

    @Operation(summary = "Register new customer", description = "Register a new customer and send verification email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody @Parameter(description = "Customer registration details") CustomerRegistrationDto registrationDto) throws SendByOpException {
        log.info("Registration dto {}");
        userRegistrationService.registerNewCustomer(registrationDto);
        return ResponseEntity.ok(new ResponseMessage("Registration successful. Please check your email for verification."));
    }

}
