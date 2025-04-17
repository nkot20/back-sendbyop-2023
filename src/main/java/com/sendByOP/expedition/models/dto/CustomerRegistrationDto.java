package com.sendByOP.expedition.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerRegistrationDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String country;
    private String address;
}