package com.sendByOP.expedition.web.exceptions;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        // Le mot de passe brut à encoder (à remplacer par le tien)
        String rawPassword = "sendbyop@2025";

        // Utilisation de BCrypt (recommandé)
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("Mot de passe brut     : " + rawPassword);
        System.out.println("Mot de passe encodé   : " + encodedPassword);
    }
}
