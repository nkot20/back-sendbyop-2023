-- ============================================
-- Migration: Amélioration de la table receiver
-- Version: V4
-- Date: 2025-10-23
-- Description: Ajout des champs pour le système de réservation amélioré
-- ============================================

-- Renommer la colonne phone en phone_number si elle existe
ALTER TABLE receiver 
CHANGE COLUMN phone phone_number VARCHAR(255) NOT NULL;

-- Ajouter les nouveaux champs d'adresse
ALTER TABLE receiver 
ADD COLUMN address VARCHAR(500) AFTER phone_number,
ADD COLUMN city VARCHAR(100) AFTER address,
ADD COLUMN country VARCHAR(100) AFTER city;

-- Ajouter le statut
ALTER TABLE receiver 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER country;

-- Ajouter les timestamps
ALTER TABLE receiver 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER status,
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Ajouter la contrainte UNIQUE sur phone_number si elle n'existe pas déjà
ALTER TABLE receiver 
ADD CONSTRAINT uk_receiver_phone UNIQUE (phone_number);

-- Ajouter la contrainte UNIQUE sur email si elle n'existe pas déjà
-- (normalement déjà présente, mais on s'assure)
ALTER TABLE receiver 
ADD CONSTRAINT uk_receiver_email UNIQUE (email);

-- Créer des index pour les recherches fréquentes
CREATE INDEX idx_receiver_status ON receiver(status);
CREATE INDEX idx_receiver_city ON receiver(city);
CREATE INDEX idx_receiver_country ON receiver(country);

-- Commentaires
ALTER TABLE receiver 
MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Statut du destinataire (ACTIVE, INACTIVE, BLOCKED)';
