package com.sendByOP.expedition.reponse;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private Integer id; // Customer ID for profile operations
    private String profilePictureUrl; // URL de la photo de profil
    private String firstName;
    private String lastName;
    private Collection<? extends GrantedAuthority> authorities;
    
    // Champs pour l'authentification à deux facteurs
    private boolean requiresOtp = false; // Indique si un code OTP est requis
    private String message; // Message pour l'utilisateur (ex: "Code OTP envoyé")

    public JwtResponse(String accessToken, String refreshToken, String username, Collection<? extends GrantedAuthority> authorities) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.authorities = authorities;
    }

    public JwtResponse(String accessToken, String refreshToken, String username, Integer customerId, 
                       String profilePictureUrl, String firstName, String lastName,
                       Collection<? extends GrantedAuthority> authorities) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.id = customerId;
        this.profilePictureUrl = profilePictureUrl;
        this.firstName = firstName;
        this.lastName = lastName;
        this.authorities = authorities;
    }
}
