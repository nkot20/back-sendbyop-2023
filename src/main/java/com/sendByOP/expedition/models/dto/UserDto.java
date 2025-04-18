package com.sendByOP.expedition.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

    private String username;
    private String email;
    private String password;
    private String lastName;
    private String firstName;
    private String roleId; // Role ID to be mapped in the DTO

}
