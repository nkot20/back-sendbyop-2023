package com.sendByOP.expedition.config;

import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.models.enums.AccountStatus;
import com.sendByOP.expedition.models.enums.RoleEnum;
import com.sendByOP.expedition.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise un compte administrateur par défaut au démarrage de l'application
 * si aucun admin n'existe.
 * 
 * Configuration requise dans .env ou application.properties:
 * - ADMIN_EMAIL: Email de l'administrateur (défaut: admin@sendbyop.net)
 * - ADMIN_PASSWORD: Mot de passe de l'administrateur (requis)
 * - ADMIN_FIRSTNAME: Prénom de l'administrateur (défaut: Admin)
 * - ADMIN_LASTNAME: Nom de l'administrateur (défaut: SendByOp)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@sendbyop.net}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Value("${admin.firstname:Admin}")
    private String adminFirstName;

    @Value("${admin.lastname:SendByOp}")
    private String adminLastName;

    @PostConstruct
    public void init() {
        log.info("🔧 Vérification de l'existence d'un compte administrateur...");

        // Vérifier si un admin existe déjà
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> RoleEnum.ADMIN.name().equals(user.getRole()));

        if (adminExists) {
            log.info("✅ Un compte administrateur existe déjà.");
            return;
        }

        // Vérifier si le mot de passe est configuré
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            log.warn("⚠️ ADMIN_PASSWORD n'est pas configuré dans .env. Aucun compte admin créé.");
            log.warn("⚠️ Pour créer un compte admin, ajoutez ADMIN_PASSWORD dans votre fichier .env");
            return;
        }

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("✅ Un utilisateur avec l'email {} existe déjà.", adminEmail);
            // Mettre à jour le rôle en ADMIN si ce n'est pas déjà le cas
            userRepository.findByEmail(adminEmail).ifPresent(user -> {
                if (!RoleEnum.ADMIN.name().equals(user.getRole())) {
                    user.setRole(RoleEnum.ADMIN.name());
                    user.setStatus(AccountStatus.ACTIVE);
                    userRepository.save(user);
                    log.info("✅ L'utilisateur {} a été promu administrateur.", adminEmail);
                }
            });
            return;
        }

        // Créer le compte admin
        try {
            User admin = User.builder()
                    .username(adminEmail)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .role(RoleEnum.ADMIN.name())
                    .status(AccountStatus.ACTIVE)
                    .twoFactorEnabled(false)
                    .build();

            userRepository.save(admin);
            
            log.info("✅ =====================================");
            log.info("✅ COMPTE ADMINISTRATEUR CRÉÉ AVEC SUCCÈS");
            log.info("✅ Email: {}", adminEmail);
            log.info("✅ Rôle: ADMIN");
            log.info("✅ =====================================");

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création du compte administrateur: {}", e.getMessage(), e);
        }
    }
}
