package com.sendByOP.expedition.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorRequest {
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "Email invalide")
    private String email;
    
    private boolean enable; // true pour activer, false pour désactiver
    
    private String otpCode; // Code OTP pour vérification
}
