package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.enums.BookingStatus;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.models.enums.PayoutStatus;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import com.sendByOP.expedition.services.iServices.IAdminPaymentService;
import com.sendByOP.expedition.services.iServices.IBookingService;
import com.sendByOP.expedition.services.iServices.ICustomerService;
import com.sendByOP.expedition.services.iServices.IUserService;
import com.sendByOP.expedition.services.impl.SecurityService;
import jakarta.validation.Valid;
import java.security.Principal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin management APIs")
@CrossOrigin(origins = "*")
public class AdminController {

    private final IUserService userService;
    private final ICustomerService customerService;
    private final IBookingService bookingService;
    private final BookingRepository bookingRepository;
    private final IAdminPaymentService adminPaymentService;
    private final SecurityService securityService;
    private final com.sendByOP.expedition.services.impl.FlightService flightService;

    @Operation(summary = "Get all users with customer info", description = "Admin endpoint to get all users with their customer information")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    @Operation(summary = "Get all customers", description = "Admin endpoint to get all customers")
    @ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getListClient());
    }

    @Operation(summary = "Block user", description = "Admin endpoint to block a user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User blocked successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/block/{email}")
    public ResponseEntity<UserDto> blockUser(
        @PathVariable("email") @Parameter(description = "User email") String email) throws SendByOpException {
        UserDto user = userService.blockUser(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Unblock user", description = "Admin endpoint to unblock a user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User unblocked successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/unblock/{email}")
    public ResponseEntity<UserDto> unblockUser(
        @PathVariable("email") @Parameter(description = "User email") String email) throws SendByOpException {
        UserDto user = userService.unblockUser(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Verify customer identity", description = "Admin endpoint to verify or unverify customer identity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Identity verification status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/customers/verify-identity/{customerId}")
    public ResponseEntity<CustomerDto> verifyCustomerIdentity(
        @PathVariable("customerId") @Parameter(description = "Customer ID") Integer customerId,
        @RequestParam @Parameter(description = "Verification status") boolean verified) throws SendByOpException {
        CustomerDto customer = customerService.verifyCustomerIdentity(customerId, verified);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Get user status by email", description = "Admin endpoint to get user account status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/status/{email}")
    public ResponseEntity<UserDto> getUserStatus(
        @PathVariable("email") @Parameter(description = "User email") String email) throws SendByOpException {
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get combined user and customer info", description = "Admin endpoint to get combined user and customer information by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User or customer not found")
    })
    @GetMapping("/users/full-info/{email}")
    public ResponseEntity<Map<String, Object>> getFullUserInfo(
        @PathVariable("email") @Parameter(description = "User email") String email) throws SendByOpException {
        UserDto user = userService.getUserByEmail(email);
        CustomerDto customer = customerService.getCustomerByEmail(email);
        
        Map<String, Object> fullInfo = new HashMap<>();
        fullInfo.put("user", user);
        fullInfo.put("customer", customer);
        
        return ResponseEntity.ok(fullInfo);
    }

    // ==========================================
    // ENDPOINTS DE GESTION DES RÉSERVATIONS (ADMIN)
    // ==========================================

    @Operation(summary = "Get all bookings with pagination", description = "Admin endpoint to get all bookings with pagination")
    @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
    @GetMapping("/bookings")
    public ResponseEntity<Page<CustomerBookingDto>> getAllBookings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search) {
        
        log.info("GET /admin/bookings - page={}, size={}, status={}, search={}", page, size, status, search);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerBookingDto> bookingsPage;
        
        if (search != null && !search.isEmpty()) {
            bookingsPage = bookingService.searchBookings(search, pageable);
        } else if (status != null && !status.isEmpty()) {
            bookingsPage = bookingService.getBookingsByStatusPaginated(status, pageable);
        } else {
            bookingsPage = bookingService.getAllBookingsPaginated(pageable);
        }
        
        return ResponseEntity.ok(bookingsPage);
    }

    @Operation(summary = "Get booking statistics", description = "Admin endpoint to get booking statistics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @GetMapping("/bookings/stats")
    public ResponseEntity<Map<String, Long>> getBookingStats() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("total", bookingRepository.count());
        stats.put("pending", bookingRepository.countByStatus(BookingStatus.PENDING_CONFIRMATION));
        stats.put("confirmed", bookingRepository.countByStatus(BookingStatus.CONFIRMED_UNPAID) + 
                               bookingRepository.countByStatus(BookingStatus.CONFIRMED_PAID));
        stats.put("paid", bookingRepository.countByStatus(BookingStatus.CONFIRMED_PAID));
        stats.put("completed", bookingRepository.countByStatus(BookingStatus.PICKED_UP));
        stats.put("cancelled", bookingRepository.countByStatus(BookingStatus.CANCELLED_BY_CLIENT) + 
                               bookingRepository.countByStatus(BookingStatus.CANCELLED_BY_TRAVELER));
        
        return ResponseEntity.ok(stats);
    }
    
    // ==================== PAYMENT ENDPOINTS ====================
    
    @Operation(summary = "Get all payments", description = "Get all payments with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/payments")
    public ResponseEntity<Page<AdminPaymentDto>> getAllPayments(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminPaymentService.getAllPayments(pageable));
    }
    
    @Operation(summary = "Get payments by status", description = "Get payments filtered by status")
    @GetMapping("/payments/status/{status}")
    public ResponseEntity<Page<AdminPaymentDto>> getPaymentsByStatus(
            @PathVariable TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminPaymentService.getPaymentsByStatus(status, pageable));
    }
    
    @Operation(summary = "Search payments", description = "Search payments by reference, customer name or email")
    @GetMapping("/payments/search")
    public ResponseEntity<Page<AdminPaymentDto>> searchPayments(
            @Parameter(description = "Search query") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminPaymentService.searchPayments(query, pageable));
    }
    
    @Operation(summary = "Get payment by ID", description = "Get payment details by ID")
    @GetMapping("/payments/{id}")
    public ResponseEntity<AdminPaymentDto> getPaymentById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminPaymentService.getPaymentById(id));
    }
    
    // ==================== PAYOUT ENDPOINTS ====================
    
    @Operation(summary = "Get all payouts", description = "Get all payouts with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payouts retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/payouts")
    public ResponseEntity<Page<AdminPayoutDto>> getAllPayouts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminPaymentService.getAllPayouts(pageable));
    }
    
    @Operation(summary = "Get payouts by status", description = "Get payouts filtered by status")
    @GetMapping("/payouts/status/{status}")
    public ResponseEntity<Page<AdminPayoutDto>> getPayoutsByStatus(
            @PathVariable PayoutStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminPaymentService.getPayoutsByStatus(status, pageable));
    }
    
    @Operation(summary = "Search payouts", description = "Search payouts by traveler name, email or booking ID")
    @GetMapping("/payouts/search")
    public ResponseEntity<Page<AdminPayoutDto>> searchPayouts(
            @Parameter(description = "Search query") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminPaymentService.searchPayouts(query, pageable));
    }
    
    @Operation(summary = "Get payout by ID", description = "Get payout details by ID")
    @GetMapping("/payouts/{id}")
    public ResponseEntity<AdminPayoutDto> getPayoutById(@PathVariable Long id) {
        return ResponseEntity.ok(adminPaymentService.getPayoutById(id));
    }
    
    @Operation(summary = "Process payout", description = "Mark a payout as completed")
    @PostMapping("/payouts/{id}/process")
    public ResponseEntity<AdminPayoutDto> processPayout(
            @PathVariable Long id,
            @RequestParam String transactionId,
            @RequestParam String paymentMethod) {
        return ResponseEntity.ok(adminPaymentService.processPayou(id, transactionId, paymentMethod));
    }
    
    // ==================== SECURITY ENDPOINTS ====================
    
    @Operation(summary = "Get admin security settings", description = "Get security settings for the authenticated admin")
    @GetMapping("/security/settings")
    public ResponseEntity<SecuritySettingsDto> getSecuritySettings(Principal principal) {
        log.info("Admin récupération des paramètres de sécurité pour {}", principal.getName());
        try {
            SecuritySettingsDto settings = securityService.getSecuritySettings(principal.getName());
            return ResponseEntity.ok(settings);
        } catch (SendByOpException e) {
            log.error("Erreur lors de la récupération des paramètres de sécurité: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Change admin password", description = "Change password for the authenticated admin")
    @PutMapping("/security/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {
        log.info("Admin changement de mot de passe pour {}", principal.getName());
        Map<String, Object> response = new HashMap<>();
        try {
            securityService.changePassword(principal.getName(), request);
            response.put("success", true);
            response.put("message", "Mot de passe changé avec succès");
            return ResponseEntity.ok(response);
        } catch (SendByOpException e) {
            log.error("Erreur lors du changement de mot de passe: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "Toggle admin 2FA", description = "Enable or disable 2FA for the authenticated admin")
    @PostMapping("/security/two-factor/toggle")
    public ResponseEntity<SecuritySettingsDto> toggleTwoFactor(
            @Valid @RequestBody Enable2FARequest request,
            Principal principal) {
        log.info("Admin {} 2FA pour {}", request.isEnable() ? "activation" : "désactivation", principal.getName());
        request.setEmail(principal.getName());
        try {
            SecuritySettingsDto settings = securityService.toggle2FA(request);
            return ResponseEntity.ok(settings);
        } catch (SendByOpException e) {
            log.error("Erreur lors de la modification du 2FA: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Send OTP to admin", description = "Send OTP code to admin email for 2FA verification")
    @PostMapping("/security/two-factor/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(Principal principal) {
        log.info("Admin envoi d'un code OTP pour {}", principal.getName());
        Map<String, Object> response = new HashMap<>();
        try {
            securityService.sendOTP(principal.getName());
            response.put("success", true);
            response.put("message", "Un code de vérification a été envoyé à votre email");
            response.put("otpSent", true);
            return ResponseEntity.ok(response);
        } catch (SendByOpException e) {
            log.error("Erreur lors de l'envoi de l'OTP: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "Confirm admin 2FA activation", description = "Confirm 2FA activation with OTP code")
    @PostMapping("/security/two-factor/confirm")
    public ResponseEntity<Map<String, Object>> confirmTwoFactorActivation(
            @Valid @RequestBody Verify2FARequest request,
            Principal principal) {
        log.info("Admin confirmation de l'activation 2FA pour {}", principal.getName());
        request.setEmail(principal.getName());
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isValid = securityService.verifyOTP(request);
            if (isValid) {
                response.put("success", true);
                response.put("message", "Authentification à deux facteurs activée avec succès");
                response.put("twoFactorEnabled", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Code de vérification invalide");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (SendByOpException e) {
            log.error("Erreur lors de la vérification du code OTP: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "Resend OTP to admin", description = "Resend OTP code to admin email")
    @PostMapping("/security/two-factor/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(Principal principal) {
        log.info("Admin renvoi de l'OTP pour {}", principal.getName());
        Map<String, Object> response = new HashMap<>();
        try {
            securityService.sendOTP(principal.getName());
            response.put("success", true);
            response.put("message", "Code de vérification renvoyé");
            return ResponseEntity.ok(response);
        } catch (SendByOpException e) {
            log.error("Erreur lors du renvoi de l'OTP: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ==================== FLIGHT VALIDATION ENDPOINTS ====================
    
    @Operation(summary = "Get pending flights", description = "Get all flights waiting for admin validation")
    @ApiResponse(responseCode = "200", description = "Pending flights retrieved successfully")
    @GetMapping("/flights/pending")
    public ResponseEntity<List<FlightDto>> getPendingFlights() {
        log.info("Admin: Récupération des vols en attente de validation");
        List<FlightDto> pendingFlights = flightService.getPendingFlights();
        return ResponseEntity.ok(pendingFlights);
    }
    
    @Operation(summary = "Validate flight", description = "Validate a pending flight and notify the traveler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Flight validated successfully"),
        @ApiResponse(responseCode = "404", description = "Flight not found"),
        @ApiResponse(responseCode = "400", description = "Flight is not pending validation")
    })
    @PostMapping("/flights/{flightId}/validate")
    public ResponseEntity<Map<String, Object>> validateFlight(
            @PathVariable Integer flightId) {
        log.info("Admin: Validation du vol ID: {}", flightId);
        Map<String, Object> response = new HashMap<>();
        try {
            FlightDto validatedFlight = flightService.validateFlight(flightId);
            response.put("success", true);
            response.put("message", "Vol validé avec succès. Le voyageur a été notifié par email.");
            response.put("flight", validatedFlight);
            return ResponseEntity.ok(response);
        } catch (SendByOpException e) {
            log.error("Erreur lors de la validation du vol {}: {}", flightId, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "Reject flight", description = "Reject a pending flight with a reason and notify the traveler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Flight rejected successfully"),
        @ApiResponse(responseCode = "404", description = "Flight not found"),
        @ApiResponse(responseCode = "400", description = "Flight is not pending validation")
    })
    @PostMapping("/flights/{flightId}/reject")
    public ResponseEntity<Map<String, Object>> rejectFlight(
            @PathVariable Integer flightId,
            @RequestBody Map<String, String> payload) {
        String reason = payload.get("reason");
        log.info("Admin: Rejet du vol ID: {} - Raison: {}", flightId, reason);
        Map<String, Object> response = new HashMap<>();
        try {
            FlightDto rejectedFlight = flightService.rejectFlight(flightId, reason);
            response.put("success", true);
            response.put("message", "Vol rejeté avec succès. Le voyageur a été notifié par email.");
            response.put("flight", rejectedFlight);
            return ResponseEntity.ok(response);
        } catch (SendByOpException e) {
            log.error("Erreur lors du rejet du vol {}: {}", flightId, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
