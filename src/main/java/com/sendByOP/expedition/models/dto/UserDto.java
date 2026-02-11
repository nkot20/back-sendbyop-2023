package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.AccountStatus;
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
    private String role;
    private AccountStatus status;

}
