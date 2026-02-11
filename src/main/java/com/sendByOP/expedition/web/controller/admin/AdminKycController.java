package com.sendByOP.expedition.web.controller.admin;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.KycDocumentDto;
import com.sendByOP.expedition.services.impl.KycService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller admin pour la gestion KYC
 */
@Slf4j
@RestController
@RequestMapping("/admin/kyc")
@RequiredArgsConstructor
@Tag(name = "Admin KYC", description = "API admin pour la validation des documents KYC")
@PreAuthorize("hasRole('ADMIN')")
public class AdminKycController {

    private final KycService kycService;

    @Operation(summary = "Get pending KYC documents", description = "Get all KYC documents pending review")
    @ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    @GetMapping("/pending")
    public ResponseEntity<Page<KycDocumentDto>> getPendingDocuments(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin getting pending KYC documents - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<KycDocumentDto> documents = kycService.getPendingDocuments(pageable);
        
        return ResponseEntity.ok(documents);
    }

    @Operation(summary = "Get customer KYC documents", description = "Get all KYC documents for a specific customer")
    @ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<KycDocumentDto>> getCustomerDocuments(
            @Parameter(description = "Customer ID") @PathVariable Integer customerId) {
        
        log.info("Admin getting KYC documents for customer: {}", customerId);
        List<KycDocumentDto> documents = kycService.getCustomerDocuments(customerId);
        
        return ResponseEntity.ok(documents);
    }

    @Operation(summary = "Approve KYC document", description = "Approve a customer's identity document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document approved successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PutMapping("/{documentId}/approve")
    public ResponseEntity<Map<String, Object>> approveDocument(
            @Parameter(description = "Document ID") @PathVariable Integer documentId,
            Principal principal) throws SendByOpException {
        
        log.info("Admin {} approving KYC document {}", principal.getName(), documentId);
        
        KycDocumentDto approved = kycService.approveKycDocument(documentId, principal.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Document approuvé avec succès");
        response.put("document", approved);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reject KYC document", description = "Reject a customer's identity document with reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Reason is required"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PutMapping("/{documentId}/reject")
    public ResponseEntity<Map<String, Object>> rejectDocument(
            @Parameter(description = "Document ID") @PathVariable Integer documentId,
            @Parameter(description = "Rejection reason") @RequestParam String reason,
            Principal principal) throws SendByOpException {
        
        log.info("Admin {} rejecting KYC document {}", principal.getName(), documentId);
        
        KycDocumentDto rejected = kycService.rejectKycDocument(documentId, reason, principal.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Document rejeté");
        response.put("document", rejected);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get KYC statistics", description = "Get KYC verification statistics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getKycStats() {
        log.info("Admin getting KYC statistics");
        
        long pendingCount = kycService.countPendingDocuments();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingReview", pendingCount);
        
        return ResponseEntity.ok(stats);
    }
}
