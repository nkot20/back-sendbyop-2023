-- V12__Add_OTP_And_TwoFactor_Columns_To_User.sql
-- Ajout des colonnes OTP et 2FA pour la sécurité dans la table user

-- Colonnes pour la réinitialisation de mot de passe
ALTER TABLE user ADD COLUMN IF NOT EXISTS otp_secret VARCHAR(255) DEFAULT NULL COMMENT 'Code OTP haché pour réinitialisation mot de passe';
ALTER TABLE user ADD COLUMN IF NOT EXISTS otp_sent_at DATETIME DEFAULT NULL COMMENT 'Date et heure d''envoi du dernier OTP';

-- Colonnes pour l'authentification à deux facteurs
ALTER TABLE user ADD COLUMN IF NOT EXISTS two_factor_enabled TINYINT(1) DEFAULT 0 COMMENT 'Indique si l''authentification à deux facteurs est activée';

-- Mettre à jour les valeurs NULL existantes pour two_factor_enabled à 0 (false)
UPDATE user SET two_factor_enabled = 0 WHERE two_factor_enabled IS NULL;

-- Créer un index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
