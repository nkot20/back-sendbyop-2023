package com.sendByOP.expedition.security.encryption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service de chiffrement AES-GCM pour les données sensibles comme les coordonnées bancaires
 * Utilise AES-256-GCM pour un chiffrement sécurisé avec authentification
 */
@Slf4j
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256; // 256 bits

    @Value("${app.encryption.secret-key}")
    private String secretKeyString;

    /**
     * Chiffre une chaîne de caractères
     * @param plainText Le texte en clair à chiffrer
     * @return Le texte chiffré encodé en Base64, ou null si l'entrée est null/vide
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            return plainText;
        }

        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // Génération d'un IV aléatoire pour chaque chiffrement
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concaténer IV + données chiffrées
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);

            String result = Base64.getEncoder().encodeToString(encryptedWithIv);
            log.debug("Successfully encrypted data of length: {}", plainText.length());
            return result;

        } catch (Exception e) {
            log.error("Error encrypting data: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Déchiffre une chaîne de caractères
     * @param encryptedText Le texte chiffré encodé en Base64
     * @return Le texte en clair déchiffré, ou null si l'entrée est null/vide
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.trim().isEmpty()) {
            return encryptedText;
        }

        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

            // Extraire l'IV et les données chiffrées
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            String result = new String(decryptedData, StandardCharsets.UTF_8);
            log.debug("Successfully decrypted data");
            return result;

        } catch (Exception e) {
            log.error("Error decrypting data: {}", e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Génère une clé secrète pour les tests ou l'initialisation
     * @return Une clé secrète encodée en Base64
     */
    public static String generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
    }

    /**
     * Récupère la clé secrète à partir de la configuration
     * @return La clé secrète
     */
    private SecretKey getSecretKey() {
        if (secretKeyString == null || secretKeyString.trim().isEmpty()) {
            throw new IllegalStateException("Encryption secret key not configured. Please set app.encryption.secret-key property.");
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
            return new SecretKeySpec(decodedKey, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid encryption secret key format", e);
        }
    }

    /**
     * Vérifie si une chaîne semble être chiffrée (format Base64 valide)
     * @param text La chaîne à vérifier
     * @return true si la chaîne semble chiffrée
     */
    public boolean isEncrypted(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(text);
            // Vérifier que la longueur est au moins IV + tag minimum
            return decoded.length >= (GCM_IV_LENGTH + GCM_TAG_LENGTH);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
