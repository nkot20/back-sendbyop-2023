package com.sendByOP.expedition.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@Schema(description = "DTO for customer information")
public class CustomerDto {
    @JsonIgnore
    @Schema(description = "Unique identifier for the customer", example = "1")
    private Integer id;

    @Schema(description = "National identification number of the customer", example = "123456789")
    private Integer nationalIdNumber;

    @Schema(description = "First name of the customer", example = "John")
    private String firstName;

    @Schema(description = "Last name of the customer", example = "Doe")
    private String lastName;

    @JsonIgnore
    @Schema(description = "Birth date of the customer", example = "1990-01-01")
    private Date birthDate;

    @JsonIgnore
    @Schema(description = "Identity document of the customer", example = "passport")
    private String identityDocument;

    @Schema(description = "Phone number of the customer", example = "+1234567890")
    private String phoneNumber;
    private String email;
    private String profilePicture;
    private int registrationStatus;
    private int emailVerified;
    private int phoneVerified;
    private String whatsappLink;
    private Date registrationDate;
    private String country;
    private int identityUploaded;
    private int identityVerified;
    @JsonIgnore
    private String iban;
    private String address;
}
