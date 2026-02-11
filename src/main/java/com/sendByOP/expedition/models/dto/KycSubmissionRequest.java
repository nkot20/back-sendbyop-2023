package com.sendByOP.expedition.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

/**
 * DTO pour la soumission d'un document KYC
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycSubmissionRequest {
    
    @NotBlank(message = "Le type de document est requis")
    private String documentType; // PASSPORT, ID_CARD, DRIVER_LICENSE
    
    private String documentNumber;
    
    @NotNull(message = "La date d'expiration est requise")
    private Date expiryDate;
    
    private String countryOfIssue;
    
    // Les images seront uploadées séparément via MultipartFile
}
