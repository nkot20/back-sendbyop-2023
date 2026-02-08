package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CustomerBookingDto;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.UserDto;
import com.sendByOP.expedition.models.enums.BookingStatus;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.services.iServices.IBookingService;
import com.sendByOP.expedition.services.iServices.ICustomerService;
import com.sendByOP.expedition.services.iServices.IUserService;
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
}
