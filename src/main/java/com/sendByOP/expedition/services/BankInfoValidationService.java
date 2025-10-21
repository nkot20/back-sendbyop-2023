package com.sendByOP.expedition.services;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.BankInfo;
import com.sendByOP.expedition.repositories.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service de validation pour les informations bancaires
 * Gère l'unicité des champs chiffrés (IBAN, BIC) au niveau applicatif
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankInfoValidationService {

    private final BankAccountRepository bankAccountRepository;

    /**
     * Valide l'unicité de l'IBAN
     * @param iban L'IBAN à valider
     * @param excludeId ID à exclure de la validation (pour les mises à jour)
     * @throws SendByOpException Si l'IBAN existe déjà
     */
    public void validateIbanUniqueness(String iban, Integer excludeId) throws SendByOpException {
        if (iban == null || iban.trim().isEmpty()) {
            return;
        }

        log.debug("Validation de l'unicité de l'IBAN");
        
        List<BankInfo> allBankInfos = bankAccountRepository.findAll();
        
        for (BankInfo bankInfo : allBankInfos) {
            // Exclure l'enregistrement actuel lors des mises à jour
            if (excludeId != null && bankInfo.getId().equals(excludeId)) {
                continue;
            }
            
            try {
                String existingIban = bankInfo.getIban();
                if (existingIban != null && iban.equals(existingIban)) {
                    log.warn("IBAN déjà existant détecté");
                    throw new SendByOpException(ErrorInfo.RESOURCE_ALREADY_EXISTS, 
                        "Cet IBAN est déjà enregistré dans le système");
                }
            } catch (Exception e) {
                // Erreur de déchiffrement - continuer avec les autres enregistrements
                log.warn("Erreur lors du déchiffrement de l'IBAN pour l'ID {}: {}", 
                    bankInfo.getId(), e.getMessage());
            }
        }
        
        log.debug("Validation de l'unicité de l'IBAN réussie");
    }

    /**
     * Valide l'unicité du BIC
     * @param bic Le BIC à valider
     * @param excludeId ID à exclure de la validation (pour les mises à jour)
     * @throws SendByOpException Si le BIC existe déjà
     */
    public void validateBicUniqueness(String bic, Integer excludeId) throws SendByOpException {
        if (bic == null || bic.trim().isEmpty()) {
            return;
        }

        log.debug("Validation de l'unicité du BIC");
        
        List<BankInfo> allBankInfos = bankAccountRepository.findAll();
        
        for (BankInfo bankInfo : allBankInfos) {
            // Exclure l'enregistrement actuel lors des mises à jour
            if (excludeId != null && bankInfo.getId().equals(excludeId)) {
                continue;
            }
            
            try {
                String existingBic = bankInfo.getBic();
                if (existingBic != null && bic.equals(existingBic)) {
                    log.warn("BIC déjà existant détecté");
                    throw new SendByOpException(ErrorInfo.RESOURCE_ALREADY_EXISTS, 
                        "Ce BIC est déjà enregistré dans le système");
                }
            } catch (Exception e) {
                // Erreur de déchiffrement - continuer avec les autres enregistrements
                log.warn("Erreur lors du déchiffrement du BIC pour l'ID {}: {}", 
                    bankInfo.getId(), e.getMessage());
            }
        }
        
        log.debug("Validation de l'unicité du BIC réussie");
    }

    /**
     * Valide toutes les contraintes d'unicité pour une information bancaire
     * @param bankInfo L'information bancaire à valider
     * @throws SendByOpException Si une contrainte d'unicité est violée
     */
    public void validateUniqueness(BankInfo bankInfo) throws SendByOpException {
        validateIbanUniqueness(bankInfo.getIban(), bankInfo.getId());
        validateBicUniqueness(bankInfo.getBic(), bankInfo.getId());
    }

    /**
     * Recherche une information bancaire par IBAN
     * @param iban L'IBAN à rechercher
     * @return L'information bancaire si trouvée
     */
    public Optional<BankInfo> findByIban(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return Optional.empty();
        }

        log.debug("Recherche d'information bancaire par IBAN");
        
        List<BankInfo> allBankInfos = bankAccountRepository.findAll();
        
        for (BankInfo bankInfo : allBankInfos) {
            try {
                String existingIban = bankInfo.getIban();
                if (existingIban != null && iban.equals(existingIban)) {
                    log.debug("Information bancaire trouvée pour l'IBAN");
                    return Optional.of(bankInfo);
                }
            } catch (Exception e) {
                log.warn("Erreur lors du déchiffrement de l'IBAN pour l'ID {}: {}", 
                    bankInfo.getId(), e.getMessage());
            }
        }
        
        log.debug("Aucune information bancaire trouvée pour l'IBAN");
        return Optional.empty();
    }

    /**
     * Recherche une information bancaire par BIC
     * @param bic Le BIC à rechercher
     * @return L'information bancaire si trouvée
     */
    public Optional<BankInfo> findByBic(String bic) {
        if (bic == null || bic.trim().isEmpty()) {
            return Optional.empty();
        }

        log.debug("Recherche d'information bancaire par BIC");
        
        List<BankInfo> allBankInfos = bankAccountRepository.findAll();
        
        for (BankInfo bankInfo : allBankInfos) {
            try {
                String existingBic = bankInfo.getBic();
                if (existingBic != null && bic.equals(existingBic)) {
                    log.debug("Information bancaire trouvée pour le BIC");
                    return Optional.of(bankInfo);
                }
            } catch (Exception e) {
                log.warn("Erreur lors du déchiffrement du BIC pour l'ID {}: {}", 
                    bankInfo.getId(), e.getMessage());
            }
        }
        
        log.debug("Aucune information bancaire trouvée pour le BIC");
        return Optional.empty();
    }

    /**
     * Valide le format d'un IBAN
     * @param iban L'IBAN à valider
     * @return true si le format est valide
     */
    public boolean isValidIbanFormat(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return false;
        }
        
        // Supprimer les espaces et convertir en majuscules
        String cleanIban = iban.replaceAll("\\s+", "").toUpperCase();
        
        // Vérification basique du format IBAN (2 lettres + 2 chiffres + jusqu'à 30 caractères alphanumériques)
        return cleanIban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$") && cleanIban.length() >= 15;
    }

    /**
     * Valide le format d'un BIC
     * @param bic Le BIC à valider
     * @return true si le format est valide
     */
    public boolean isValidBicFormat(String bic) {
        if (bic == null || bic.trim().isEmpty()) {
            return false;
        }
        
        // Supprimer les espaces et convertir en majuscules
        String cleanBic = bic.replaceAll("\\s+", "").toUpperCase();
        
        // Vérification du format BIC (8 ou 11 caractères)
        return cleanBic.matches("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    }
}
