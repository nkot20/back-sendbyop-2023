package com.sendByOP.expedition.models.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class CustomerDto {
    private Integer id;
    private Integer nationalIdNumber;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String identityDocument;
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
    private String iban;
    private String address;
}
