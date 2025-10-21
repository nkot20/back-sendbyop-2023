package com.sendByOP.expedition.utils;

import com.sendByOP.expedition.security.encryption.EncryptionService;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitaire pour générer des clés de chiffrement
 * Utilisé pour l'initialisation et les tests
 */
@Slf4j
public class EncryptionKeyGenerator {

    /**
     * Génère et affiche une nouvelle clé de chiffrement
     * Utilisez cette méthode pour générer une clé sécurisée pour la production
     */
    public static void main(String[] args) {
        try {
            String secretKey = EncryptionService.generateSecretKey();
            System.out.println("=".repeat(80));
            System.out.println("NOUVELLE CLÉ DE CHIFFREMENT GÉNÉRÉE");
            System.out.println("=".repeat(80));
            System.out.println();
            System.out.println("Clé à utiliser dans application.properties :");
            System.out.println("app.encryption.secret-key=" + secretKey);
            System.out.println();
            System.out.println("IMPORTANT :");
            System.out.println("- Gardez cette clé secrète et sécurisée");
            System.out.println("- Sauvegardez-la dans un gestionnaire de secrets");
            System.out.println("- Ne la commitez jamais dans le code source");
            System.out.println("- Utilisez des variables d'environnement en production");
            System.out.println();
            System.out.println("=".repeat(80));
            
            log.info("Clé de chiffrement générée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la génération de la clé de chiffrement", e);
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
