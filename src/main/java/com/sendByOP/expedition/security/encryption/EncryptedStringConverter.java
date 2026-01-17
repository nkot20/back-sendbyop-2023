package com.sendByOP.expedition.security.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Convertisseur JPA pour chiffrer/déchiffrer automatiquement les chaînes de caractères
 * Utilisé pour les champs sensibles comme les coordonnées bancaires
 */
@Slf4j
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    public  EncryptedStringConverter(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    /**
     * Convertit la valeur de l'entité (texte clair) vers la base de données (texte chiffré)
     * @param attribute La valeur en clair de l'attribut
     * @return La valeur chiffrée pour la base de données
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (encryptionService == null) {
            log.warn("EncryptionService not available, storing data unencrypted");
            return attribute;
        }

        if (attribute == null || attribute.trim().isEmpty()) {
            return attribute;
        }

        try {
            // Si déjà chiffré, ne pas rechiffrer
            if (encryptionService.isEncrypted(attribute)) {
                log.debug("Data appears to already be encrypted, skipping encryption");
                return attribute;
            }

            String encrypted = encryptionService.encrypt(attribute);
            log.debug("Successfully encrypted attribute for database storage");
            return encrypted;
        } catch (Exception e) {
            log.error("Failed to encrypt attribute, storing unencrypted: {}", e.getMessage());
            return attribute;
        }
    }

    /**
     * Convertit la valeur de la base de données (texte chiffré) vers l'entité (texte clair)
     * @param dbData La valeur chiffrée de la base de données
     * @return La valeur en clair pour l'entité
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (encryptionService == null) {
            log.warn("EncryptionService not available, returning data as-is");
            return dbData;
        }

        if (dbData == null || dbData.trim().isEmpty()) {
            return dbData;
        }

        try {
            // Vérifier si les données semblent chiffrées
            if (!encryptionService.isEncrypted(dbData)) {
                log.debug("Data appears to be unencrypted, returning as-is");
                return dbData;
            }

            String decrypted = encryptionService.decrypt(dbData);
            log.debug("Successfully decrypted attribute from database");
            return decrypted;
        } catch (Exception e) {
            log.error("Failed to decrypt attribute, returning encrypted data: {}", e.getMessage());
            return dbData;
        }
    }
}
