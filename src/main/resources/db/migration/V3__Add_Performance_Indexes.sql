-- ============================================
-- Migration: Ajout d'index pour optimisation performance
-- Version: V3
-- Date: 2025-10-23
-- Description: Ajoute des index sur les colonnes fréquemment recherchées
--              pour améliorer les performances des requêtes
-- ============================================

-- Index sur la table FLIGHT
-- ============================================

-- Index sur validation_status (utilisé dans getAllVolValid)
CREATE INDEX IF NOT EXISTS idx_flight_validation_status 
ON flight(validation_status);

-- Index sur departure_date (utilisé dans les tris)
CREATE INDEX IF NOT EXISTS idx_flight_departure_date 
ON flight(departure_date DESC);

-- Index sur customer_id (utilisé dans les JOIN et recherches par voyageur)
CREATE INDEX IF NOT EXISTS idx_flight_customer 
ON flight(customer_id);

-- Index sur cancelled (utilisé pour filtrer les vols actifs)
CREATE INDEX IF NOT EXISTS idx_flight_cancelled 
ON flight(cancelled);

-- Index composite pour requête fréquente: vols actifs triés par date
CREATE INDEX IF NOT EXISTS idx_flight_status_date 
ON flight(validation_status, departure_date DESC, cancelled);

-- Index sur airport_departure_id et airport_arrival_id (pour recherche par aéroport)
CREATE INDEX IF NOT EXISTS idx_flight_departure_airport 
ON flight(airport_departure_id);

CREATE INDEX IF NOT EXISTS idx_flight_arrival_airport 
ON flight(airport_arrival_id);

-- Index sur la table BOOKING (RESERVATION)
-- ============================================

-- Index sur customer_id (pour rechercher les réservations d'un client)
CREATE INDEX IF NOT EXISTS idx_booking_customer 
ON booking(customer_id);

-- Index sur flight_id (pour rechercher les réservations d'un vol)
CREATE INDEX IF NOT EXISTS idx_booking_flight 
ON booking(flight_id);

-- Index sur status (pour filtrer par statut de réservation)
CREATE INDEX IF NOT EXISTS idx_booking_status 
ON booking(status);

-- Index composite pour requêtes fréquentes
CREATE INDEX IF NOT EXISTS idx_booking_customer_status 
ON booking(customer_id, status);

-- Index sur created_at pour tri chronologique
CREATE INDEX IF NOT EXISTS idx_booking_created_at 
ON booking(created_at DESC);

-- Index sur la table USER
-- ============================================

-- Index sur email (utilisé dans findByEmail - UNIQUE déjà existant normalement)
-- CREATE UNIQUE INDEX IF NOT EXISTS idx_user_email ON user(email);

-- Index sur status (pour filtrer par statut de compte)
CREATE INDEX IF NOT EXISTS idx_user_status 
ON user(status);

-- Index sur role (pour filtrer par rôle)
CREATE INDEX IF NOT EXISTS idx_user_role 
ON user(role);

-- Index sur la table CUSTOMER
-- ============================================

-- Index sur email (utilisé dans findByEmail)
-- Devrait être UNIQUE suite à V2__Add_Unique_Constraint_Customer_Email.sql
-- CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_email ON customer(email);

-- Index sur phone_number (utilisé dans les recherches)
CREATE INDEX IF NOT EXISTS idx_customer_phone 
ON customer(phone_number);

-- Index sur email_verified (pour filtrer les comptes vérifiés)
CREATE INDEX IF NOT EXISTS idx_customer_email_verified 
ON customer(email_verified);

-- Index sur la table PAYMENT
-- ============================================

-- Index sur booking_id (pour rechercher les paiements d'une réservation)
CREATE INDEX IF NOT EXISTS idx_payment_booking 
ON payment(booking_id);

-- Index sur payment_status (pour filtrer par statut de paiement)
CREATE INDEX IF NOT EXISTS idx_payment_status 
ON payment(payment_status);

-- Index sur payment_date (pour tri chronologique)
CREATE INDEX IF NOT EXISTS idx_payment_date 
ON payment(payment_date DESC);

-- Index sur la table PARCEL
-- ============================================

-- Index sur booking_id (pour rechercher les colis d'une réservation)
CREATE INDEX IF NOT EXISTS idx_parcel_booking 
ON parcel(booking_id);

-- Index sur la table STOPOVER (ESCALE)
-- ============================================

-- Index sur flight_id (pour rechercher les escales d'un vol)
CREATE INDEX IF NOT EXISTS idx_stopover_flight 
ON stopover(flight_id);

-- Index sur airport_id (pour rechercher les escales par aéroport)
CREATE INDEX IF NOT EXISTS idx_stopover_airport 
ON stopover(airport_id);

-- Index sur la table REVIEW (AVIS)
-- ============================================

-- Index sur customer_id (pour rechercher les avis d'un client)
CREATE INDEX IF NOT EXISTS idx_review_customer 
ON review(customer_id);

-- Index sur flight_id (pour rechercher les avis d'un vol)
CREATE INDEX IF NOT EXISTS idx_review_flight 
ON review(flight_id);

-- Index sur created_at (pour tri chronologique)
CREATE INDEX IF NOT EXISTS idx_review_created_at 
ON review(created_at DESC);

-- ============================================
-- ANALYSER LES TABLES APRÈS CRÉATION DES INDEX
-- ============================================
-- Cela met à jour les statistiques MySQL/PostgreSQL
-- pour optimiser le query planner

ANALYZE TABLE flight;
ANALYZE TABLE booking;
ANALYZE TABLE user;
ANALYZE TABLE customer;
ANALYZE TABLE payment;
ANALYZE TABLE parcel;
ANALYZE TABLE stopover;
ANALYZE TABLE review;

-- ============================================
-- NOTES
-- ============================================
-- 1. IF NOT EXISTS : Évite les erreurs si l'index existe déjà
-- 2. Index composites : Ordre des colonnes important (plus sélectif en premier)
-- 3. DESC sur dates : Optimise les tris décroissants
-- 4. ANALYZE : Met à jour les statistiques pour le query optimizer
--
-- MONITORING:
-- - MySQL: SHOW INDEX FROM flight;
-- - MySQL: EXPLAIN SELECT * FROM flight WHERE validation_status = 1;
-- - PostgreSQL: \di (liste les index)
-- - PostgreSQL: EXPLAIN ANALYZE SELECT * FROM flight WHERE validation_status = 1;
--
-- MAINTENANCE:
-- - Reconstruire index si fragmentés: ALTER TABLE flight ENGINE=InnoDB;
-- - Supprimer index inutilisés: DROP INDEX idx_name ON table_name;
-- ============================================
