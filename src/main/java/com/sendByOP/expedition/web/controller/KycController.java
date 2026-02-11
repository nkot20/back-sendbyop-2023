package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.KycDocumentDto;
import com.sendByOP.expedition.models.dto.KycSubmissionRequest;
import com.sendByOP.expedition.services.impl.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion KYC côté client
 */
@Slf4j
@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "API de vérification d'identité (KYC)")
public class KycController {

    private final KycService kycService;
    private final com.sendByOP.expedition.services.iServices.ICustomerService customerService;

    @Operation(summary = "Submit KYC document", description = "Submit identity document for verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> submitKycDocument(
            @Parameter(description = "Document type") @RequestParam String documentType,
            @Parameter(description = "Document number") @RequestParam(required = false) String documentNumber,
            @Parameter(description = "Expiry date (yyyy-MM-dd)") @RequestParam String expiryDate,
            @Parameter(description = "Country of issue") @RequestParam(required = false) String countryOfIssue,
            @Parameter(description = "Front image of document") @RequestParam("frontImage") MultipartFile frontImage,
            @Parameter(description = "Back image of document (optional)") @RequestParam(value = "backImage", required = false) MultipartFile backImage,
            Principal principal) throws SendByOpException {

        log.info("KYC submission request from user: {}", principal.getName());

        // Récupérer l'ID du client depuis le principal
        // TODO: Adapter selon votre implémentation d'authentification
        Integer customerId = getCustomerIdFromPrincipal(principal);

        // Créer la requête
        KycSubmissionRequest request = KycSubmissionRequest.builder()
                .documentType(documentType)
                .documentNumber(documentNumber)
                .expiryDate(java.sql.Date.valueOf(expiryDate))
                .countryOfIssue(countryOfIssue)
                .build();

        // Soumettre le document
        KycDocumentDto result = kycService.submitKycDocument(customerId, request, frontImage, backImage);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Document soumis avec succès. Il sera vérifié sous 24-48h.");
        response.put("document", result);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get KYC status", description = "Get current KYC verification status")
    @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getKycStatus(Principal principal) throws SendByOpException {
        log.info("Getting KYC status for user: {}", principal.getName());

        Integer customerId = getCustomerIdFromPrincipal(principal);
        KycDocumentDto status = kycService.getCustomerKycStatus(customerId);

        Map<String, Object> response = new HashMap<>();
        response.put("hasDocument", status != null);
        response.put("document", status);
        response.put("isVerified", status != null && status.isValid());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get KYC history", description = "Get all KYC documents submitted by the user")
    @ApiResponse(responseCode = "200", description = "History retrieved successfully")
    @GetMapping("/history")
    public ResponseEntity<List<KycDocumentDto>> getKycHistory(Principal principal) throws SendByOpException {
        log.info("Getting KYC history for user: {}", principal.getName());

        Integer customerId = getCustomerIdFromPrincipal(principal);
        List<KycDocumentDto> history = kycService.getCustomerDocuments(customerId);

        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Check if KYC is required", description = "Check if user needs to submit KYC")
    @ApiResponse(responseCode = "200", description = "Check completed")
    @GetMapping("/required")
    public ResponseEntity<Map<String, Object>> isKycRequired(Principal principal) throws SendByOpException {
        Integer customerId = getCustomerIdFromPrincipal(principal);
        boolean hasValidKyc = kycService.hasValidKyc(customerId);

        Map<String, Object> response = new HashMap<>();
        response.put("required", !hasValidKyc);
        response.put("hasValidKyc", hasValidKyc);

        return ResponseEntity.ok(response);
    }

    // Helper method - récupère l'ID du client depuis le principal (email)
    private Integer getCustomerIdFromPrincipal(Principal principal) throws SendByOpException {
        if (principal == null) {
            log.error("Principal is null");
            throw new SendByOpException(com.sendByOP.expedition.exception.ErrorInfo.UNAUTHORIZED, "Non authentifié");
        }
        
        String email = principal.getName(); // Le principal contient l'email de l'utilisateur
        log.debug("Retrieving customer ID for email: {}", email);
        
        // Récupérer le client depuis l'email via CustomerService
        com.sendByOP.expedition.models.dto.CustomerDto customer = customerService.getCustomerByEmail(email);
        
        if (customer == null || customer.getId() == null) {
            log.error("Customer not found or has no ID for email: {}", email);
            throw new SendByOpException(
                com.sendByOP.expedition.exception.ErrorInfo.RESOURCE_NOT_FOUND,
                "Client non trouvé pour l'email: " + email
            );
        }
        
        log.debug("Found customer ID {} for email {}", customer.getId(), email);
        return customer.getId();
    }
}
