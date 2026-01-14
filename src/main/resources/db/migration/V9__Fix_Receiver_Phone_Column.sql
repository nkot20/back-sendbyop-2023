-- ============================================
-- Migration: Correction de la colonne phone dans receiver
-- Version: V9
-- Date: 2026-01-08
-- Description: Supprime l'ancienne colonne 'phone' si elle existe encore
-- ============================================

-- La migration V4 aurait dû renommer 'phone' en 'phone_number'
-- Mais si les deux colonnes existent, on supprime 'phone'
-- Cette commande supprime la colonne phone si elle existe

ALTER TABLE receiver DROP COLUMN IF EXISTS phone;
