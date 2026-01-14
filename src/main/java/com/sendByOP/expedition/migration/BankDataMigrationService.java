package com.sendByOP.expedition.migration;

import com.sendByOP.expedition.models.entities.BankInfo;
import com.sendByOP.expedition.repositories.BankAccountRepository;
import com.sendByOP.expedition.security.encryption.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de migration pour chiffrer les données bancaires existantes
 * Activé uniquement si la propriété app.migration.encrypt-bank-data=true
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.migration.encrypt-bank-data", havingValue = "true")
public class BankDataMigrationService implements CommandLineRunner {

    private final BankAccountRepository bankAccountRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=".repeat(80));
        log.info("DÉMARRAGE DE LA MIGRATION DES DONNÉES BANCAIRES");
        log.info("=".repeat(80));

        try {
            List<BankInfo> allBankInfos = bankAccountRepository.findAll();
            log.info("Nombre total d'enregistrements à traiter : {}", allBankInfos.size());

            if (allBankInfos.isEmpty()) {
                log.info("Aucune donnée bancaire à migrer");
                return;
            }

            int processed = 0;
            int alreadyEncrypted = 0;
            int errors = 0;

            for (BankInfo bankInfo : allBankInfos) {
                try {
                    boolean needsUpdate = false;

                    // Vérifier et chiffrer l'IBAN si nécessaire
                    if (bankInfo.getIban() != null && !encryptionService.isEncrypted(bankInfo.getIban())) {
                        log.debug("Chiffrement de l'IBAN pour l'ID : {}", bankInfo.getId());
                        String encryptedIban = encryptionService.encrypt(bankInfo.getIban());
                        bankInfo.setIban(encryptedIban);
                        needsUpdate = true;
                    }

                    // Vérifier et chiffrer le numéro de compte si nécessaire
                    if (bankInfo.getBankAccount() != null && !encryptionService.isEncrypted(bankInfo.getBankAccount())) {
                        log.debug("Chiffrement du numéro de compte pour l'ID : {}", bankInfo.getId());
                        String encryptedAccount = encryptionService.encrypt(bankInfo.getBankAccount());
                        bankInfo.setBankAccount(encryptedAccount);
                        needsUpdate = true;
                    }

                    // Vérifier et chiffrer le BIC si nécessaire
                    if (bankInfo.getBic() != null && !encryptionService.isEncrypted(bankInfo.getBic())) {
                        log.debug("Chiffrement du BIC pour l'ID : {}", bankInfo.getId());
                        String encryptedBic = encryptionService.encrypt(bankInfo.getBic());
                        bankInfo.setBic(encryptedBic);
                        needsUpdate = true;
                    }

                    // Vérifier et chiffrer le titulaire du compte si nécessaire
                    if (bankInfo.getAccountHolder() != null && !encryptionService.isEncrypted(bankInfo.getAccountHolder())) {
                        log.debug("Chiffrement du titulaire pour l'ID : {}", bankInfo.getId());
                        String encryptedHolder = encryptionService.encrypt(bankInfo.getAccountHolder());
                        bankInfo.setAccountHolder(encryptedHolder);
                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        bankAccountRepository.save(bankInfo);
                        processed++;
                        log.debug("Enregistrement {} migré avec succès", bankInfo.getId());
                    } else {
                        alreadyEncrypted++;
                        log.debug("Enregistrement {} déjà chiffré", bankInfo.getId());
                    }

                } catch (Exception e) {
                    errors++;
                    log.error("Erreur lors de la migration de l'enregistrement ID {}: {}", 
                            bankInfo.getId(), e.getMessage());
                }
            }

            log.info("=".repeat(80));
            log.info("MIGRATION TERMINÉE");
            log.info("Total des enregistrements : {}", allBankInfos.size());
            log.info("Enregistrements chiffrés : {}", processed);
            log.info("Déjà chiffrés : {}", alreadyEncrypted);
            log.info("Erreurs : {}", errors);
            log.info("=".repeat(80));

            if (errors > 0) {
                log.warn("La migration s'est terminée avec {} erreurs. Vérifiez les logs pour plus de détails.", errors);
            } else {
                log.info("Migration réussie sans erreurs !");
            }

        } catch (Exception e) {
            log.error("Erreur critique lors de la migration des données bancaires", e);
            throw e;
        }
    }
}
