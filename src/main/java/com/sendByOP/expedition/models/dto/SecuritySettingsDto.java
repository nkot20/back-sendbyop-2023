package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySettingsDto {
    private String email;
    private Boolean twoFactorEnabled;
    private Boolean emailVerified;
    private Boolean phoneVerified;
}
