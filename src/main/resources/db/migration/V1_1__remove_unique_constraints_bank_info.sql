-- Migration pour supprimer les contraintes d'unicité sur les champs chiffrés
-- Les contraintes d'unicité ne fonctionnent plus avec le chiffrement car chaque valeur
-- produit un chiffrement différent (IV aléatoire)

-- Supprimer la contrainte d'unicité sur l'IBAN
ALTER TABLE bank_info DROP INDEX IF EXISTS UK_bank_info_iban;
ALTER TABLE bank_info DROP CONSTRAINT IF EXISTS UK_bank_info_iban;

-- Supprimer la contrainte d'unicité sur le BIC  
ALTER TABLE bank_info DROP INDEX IF EXISTS UK_bank_info_bic;
ALTER TABLE bank_info DROP CONSTRAINT IF EXISTS UK_bank_info_bic;

-- Note: L'unicité sera maintenant gérée au niveau applicatif via BankInfoValidationService
