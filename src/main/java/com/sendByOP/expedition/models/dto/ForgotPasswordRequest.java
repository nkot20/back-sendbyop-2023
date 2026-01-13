package com.sendByOP.expedition.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la demande de réinitialisation de mot de passe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    private String email;
}
