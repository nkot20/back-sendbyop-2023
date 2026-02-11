package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.KycStatus;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO pour les documents KYC
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDocumentDto implements Serializable {
    
    private Integer id;
    private Integer customerId;
    private String customerEmail;
    private String customerName;
    private String documentType;
    private String documentNumber;
    private String frontImagePath;
    private String backImagePath;
    private KycStatus status;
    private String rejectionReason;
    private Date submittedAt;
    private Date reviewedAt;
    private String reviewedBy;
    private Date expiryDate;
    private String countryOfIssue;
    private boolean expired;
    private boolean valid;
}
