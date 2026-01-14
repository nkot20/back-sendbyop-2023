package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.RecipientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO pour les destinataires de colis
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ReceiverDto implements Serializable {

    private Integer id;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;
    
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phoneNumber;
    
    private String address;
    private String city;
    private String country;
    
    private RecipientStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
