-- V10__Add_TwoFactor_Auth_Columns.sql
-- Ajout des colonnes pour l'authentification à deux facteurs

-- Ajouter la colonne si elle n'existe pas déjà
ALTER TABLE customer ADD COLUMN IF NOT EXISTS two_factor_enabled TINYINT(1) DEFAULT 0 COMMENT 'Indique si l''authentification à deux facteurs est activée';
ALTER TABLE customer ADD COLUMN IF NOT EXISTS otp_secret VARCHAR(255) DEFAULT NULL COMMENT 'Code OTP actuel (stocké de manière temporaire)';
ALTER TABLE customer ADD COLUMN IF NOT EXISTS otp_sent_at DATETIME DEFAULT NULL COMMENT 'Date et heure d''envoi du dernier OTP';

-- Mettre à jour les valeurs NULL existantes
UPDATE customer SET two_factor_enabled = 0 WHERE two_factor_enabled IS NULL;
