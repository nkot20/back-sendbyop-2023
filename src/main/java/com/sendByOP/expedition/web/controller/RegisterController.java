package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.CustomerRegistrationDto;
import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.impl.UserRegistrationService;
import com.sendByOP.expedition.services.impl.PasswordResetService;
import com.sendByOP.expedition.services.iServices.IClientServivce;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@Tag(name = "Customer Registration", description = "API endpoints for customer registration and management")
public class RegisterController{
    private final IClientServivce customerService;
    private final UserRegistrationService userRegistrationService;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "Get all customers", description = "Retrieves a list of all registered customers")
    @ApiResponse(responseCode = "200", description = "List of customers retrieved successfully",
            content = @Content(schema = @Schema(implementation = CustomerDto.class)))
    @GetMapping("/")
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getListClient());
    }


    @Operation(summary = "Update customer details", description = "Update an existing customer's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/update")
    public ResponseEntity<?> updateCustomer(@RequestBody @Parameter(description = "Updated customer details") CustomerDto customer) throws SendByOpException {
        CustomerDto updatedCustomer = customerService.updateClient(customer);
        return ResponseEntity.ok(updatedCustomer);
    }

    @Operation(summary = "Resend verification email", description = "Resend the verification email to a registered customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification email sent successfully"),
        @ApiResponse(responseCode = "404", description = "Email not found")
    })
    @PostMapping("/resend/email/{email}")
    public ResponseEntity<?> resendVerificationEmail(@PathVariable("email") @Parameter(description = "Customer's email address") String email) throws SendByOpException {
        userRegistrationService.resendVerificationEmail(email);
        return ResponseEntity.ok(new ResponseMessage("Verification email sent successfully"));
    }

    @Operation(summary = "Get customer by verification token", description = "Retrieve customer details using verification token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Invalid or expired token")
    })
    @PostMapping("/verify/customer/{token}")
    public ResponseEntity<?> getCustomerByToken(@PathVariable("token") @Parameter(description = "Verification token") String token) throws SendByOpException {
        CustomerDto customer = userRegistrationService.verifyCustomerEmail(token);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Verify email address", description = "Verify customer's email address using verification token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/verify/email/{token}")
    public ResponseEntity<?> verifyEmail(@PathVariable("token") @Parameter(description = "Email verification token") String token) throws SendByOpException {
        userRegistrationService.verifyCustomerEmail(token);
        return ResponseEntity.ok(new ResponseMessage("Email verified successfully"));
    }

    @Operation(summary = "Request password reset", description = "Send password reset email to customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
        @ApiResponse(responseCode = "404", description = "Email not found")
    })
    @PostMapping("/password/forgot")
    public ResponseEntity<?> sendPasswordResetEmail(@RequestBody @Parameter(description = "Customer's email") EmailDto emailDto) throws SendByOpException {
        passwordResetService.initiatePasswordReset(emailDto.getTo());
        return ResponseEntity.ok(new ResponseMessage("Password reset email sent successfully"));
    }

    @Operation(summary = "Verify password reset token", description = "Verify if password reset token is valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/password/verify-token")
    public ResponseEntity<?> verifyPasswordResetToken(@RequestBody @Parameter(description = "Password reset token") String token) throws SendByOpException {
        passwordResetService.verifyResetToken(token);
        return ResponseEntity.ok(new ResponseMessage("Token verified successfully"));
    }

    @Operation(summary = "Reset password", description = "Reset customer's password using reset token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token or password")
    })
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
        @RequestParam @Parameter(description = "Password reset token") String token,
        @RequestBody @Parameter(description = "New password") String newPassword) throws SendByOpException {
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok(new ResponseMessage("Password reset successfully"));
    }
}
