-- Migration V17: Ajout des paramètres de protection anti-fraude
-- Limite le nombre de réservations et voyages par utilisateur par semaine

-- Ajouter les colonnes de limites anti-fraude
ALTER TABLE platform_settings 
ADD COLUMN IF NOT EXISTS max_bookings_per_week INT NOT NULL DEFAULT 2;

ALTER TABLE platform_settings 
ADD COLUMN IF NOT EXISTS max_flights_per_week INT NOT NULL DEFAULT 2;

ALTER TABLE platform_settings 
ADD COLUMN IF NOT EXISTS fraud_protection_enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- Mettre à jour l'enregistrement existant avec les valeurs par défaut
UPDATE platform_settings 
SET max_bookings_per_week = 2, 
    max_flights_per_week = 2, 
    fraud_protection_enabled = TRUE
WHERE max_bookings_per_week IS NULL OR max_flights_per_week IS NULL;
