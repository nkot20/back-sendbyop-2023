package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.services.iServices.ICustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile Management", description = "APIs for managing user profile including profile picture upload")
public class ProfileController {

    private final ICustomerService customerService;

    @PostMapping(value = "/upload-picture/{customerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile picture", 
               description = "Upload a secure profile picture for a customer. Supports JPEG, PNG, and WebP formats up to 5MB.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation error"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "413", description = "File size too large"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> uploadProfilePicture(
            @PathVariable("customerId") @Parameter(description = "Customer ID") Integer customerId,
            @RequestParam("file") @Parameter(description = "Profile picture file (JPEG, PNG, WebP, max 5MB)") MultipartFile file) {
        
        log.info("Received profile picture upload request for customer: {}", customerId);
        
        try {
            String filename = customerService.uploadProfilePicture(customerId, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile picture uploaded successfully");
            response.put("filename", filename);
            response.put("customerId", customerId);
            
            log.info("Profile picture uploaded successfully for customer: {}, filename: {}", customerId, filename);
            return ResponseEntity.ok(response);
            
        } catch (SendByOpException ex) {
            log.error("Failed to upload profile picture for customer {}: {}", customerId, ex.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("customerId", customerId);
            
            return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
        } catch (Exception ex) {
            log.error("Unexpected error uploading profile picture for customer {}: {}", customerId, ex.getMessage(), ex);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "An unexpected error occurred. Please try again.");
            errorResponse.put("customerId", customerId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/delete-picture/{customerId}")
    @Operation(summary = "Delete profile picture", 
               description = "Delete the current profile picture for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> deleteProfilePicture(
            @PathVariable("customerId") @Parameter(description = "Customer ID") Integer customerId) {
        
        log.info("Received profile picture deletion request for customer: {}", customerId);
        
        try {
            // This would require implementing a delete method in the service
            // For now, we can upload an empty/null file to effectively "delete"
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile picture deletion endpoint - implementation pending");
            response.put("customerId", customerId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            log.error("Unexpected error deleting profile picture for customer {}: {}", customerId, ex.getMessage(), ex);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "An unexpected error occurred. Please try again.");
            errorResponse.put("customerId", customerId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
