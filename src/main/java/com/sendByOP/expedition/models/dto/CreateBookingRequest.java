package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * DTO pour la création d'une réservation de transport de colis
 * Contient toutes les informations nécessaires pour créer une réservation
 */
@Schema(description = "Requête de création d'une réservation de transport de colis")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    // ==========================================
    // VOL
    // ==========================================
    
    @Schema(description = "ID du vol sur lequel transporter le colis", example = "42", required = true)
    @NotNull(message = "L'ID du vol est requis")
    private Integer flightId;
    
    // ==========================================
    // DESTINATAIRE
    // ==========================================
    
    @Schema(description = "Prénom du destinataire du colis", example = "Marie", required = true)
    @NotBlank(message = "Le prénom du destinataire est requis")
    private String receiverFirstName;
    
    @Schema(description = "Nom de famille du destinataire", example = "Dubois", required = true)
    @NotBlank(message = "Le nom du destinataire est requis")
    private String receiverLastName;
    
    @Schema(description = "Adresse email du destinataire (au moins l'email OU le téléphone requis)", example = "marie.dubois@example.com")
    @Email(message = "L'email du destinataire doit être valide")
    private String receiverEmail;
    
    @Schema(description = "Numéro de téléphone du destinataire (format international recommandé)", example = "+33612345678")
    private String receiverPhoneNumber;
    
    @Schema(description = "Adresse complète de livraison", example = "15 Rue de la Paix")
    private String receiverAddress;
    
    @Schema(description = "Ville de livraison", example = "Paris")
    private String receiverCity;
    
    @Schema(description = "Pays de livraison", example = "France")
    private String receiverCountry;
    
    // ==========================================
    // COLIS
    // ==========================================
    
    @Schema(description = "Poids du colis en kilogrammes", example = "5.5", minimum = "0.1", maximum = "100.0", required = true)
    @NotNull(message = "Le poids du colis est requis")
    @DecimalMin(value = "0.1", message = "Le poids minimum est de 0.1 kg")
    @DecimalMax(value = "100.0", message = "Le poids maximum est de 100 kg")
    private BigDecimal parcelWeight;
    
    @Schema(description = "Longueur du colis en centimètres", example = "30.0")
    @DecimalMin(value = "0.0", message = "La longueur doit être positive")
    private BigDecimal parcelLength;
    
    @Schema(description = "Largeur du colis en centimètres", example = "20.0")
    @DecimalMin(value = "0.0", message = "La largeur doit être positive")
    private BigDecimal parcelWidth;
    
    @Schema(description = "Hauteur du colis en centimètres", example = "15.0")
    @DecimalMin(value = "0.0", message = "La hauteur doit être positive")
    private BigDecimal parcelHeight;
    
    @Schema(description = "Description détaillée du contenu du colis (entre 10 et 500 caractères)", 
            example = "Vêtements et accessoires pour bébé : bodys, pyjamas, jouets en peluche", 
            minLength = 10, maxLength = 500, required = true)
    @NotBlank(message = "La description du colis est requise")
    @Size(min = 10, max = 500, message = "La description doit contenir entre 10 et 500 caractères")
    private String parcelDescription;
    
    @Schema(description = "Catégorie du colis", example = "Vêtements", 
            allowableValues = {"Vêtements", "Électronique", "Documents", "Nourriture", "Médicaments", "Cosmétiques", "Jouets", "Autre"})
    private String parcelCategory;
    
    // ==========================================
    // PHOTO DU COLIS
    // ==========================================
    
    @Schema(description = "URL de la photo du colis (géré automatiquement après upload)", accessMode = Schema.AccessMode.READ_ONLY, hidden = true)
    // Note: MultipartFile sera géré séparément dans le controller
    // Ici on stockera juste l'URL après upload
    private String parcelPhotoUrl;
    
    // ==========================================
    // PRIX PROPOSÉ (optionnel, sera calculé si non fourni)
    // ==========================================
    
    @Schema(description = "Prix proposé par le client en euros (optionnel, sera calculé automatiquement si non fourni)", 
            example = "25.50", minimum = "0.0")
    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    private BigDecimal proposedPrice;
    
    // ==========================================
    // VALIDATION MÉTIER
    // ==========================================
    
    /**
     * Vérifie qu'au moins l'email OU le téléphone est fourni
     */
    @AssertTrue(message = "L'email ou le numéro de téléphone du destinataire est requis")
    public boolean isReceiverContactValid() {
        return (receiverEmail != null && !receiverEmail.trim().isEmpty()) ||
               (receiverPhoneNumber != null && !receiverPhoneNumber.trim().isEmpty());
    }
}
