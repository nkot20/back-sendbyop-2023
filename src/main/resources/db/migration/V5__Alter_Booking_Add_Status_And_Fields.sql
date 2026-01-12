-- ============================================
-- Migration: Amélioration de la table booking
-- Version: V5
-- Date: 2025-10-23
-- Description: Ajout du système de statut enum et nouveaux champs
-- ============================================

-- Ajouter le statut enum
ALTER TABLE booking 
ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING_CONFIRMATION' AFTER id;

-- Ajouter les timestamps de cycle de vie
ALTER TABLE booking 
ADD COLUMN confirmed_at DATETIME NULL AFTER booking_time,
ADD COLUMN paid_at DATETIME NULL AFTER confirmed_at,
ADD COLUMN payment_deadline DATETIME NULL AFTER paid_at,
ADD COLUMN delivered_at DATETIME NULL AFTER payment_deadline,
ADD COLUMN cancelled_at DATETIME NULL AFTER delivered_at;

-- Ajouter le champ pour la photo du colis
ALTER TABLE booking 
ADD COLUMN parcel_photo_url VARCHAR(500) NULL AFTER cancelled_at;

-- Ajouter les montants (BigDecimal -> DECIMAL)
ALTER TABLE booking 
ADD COLUMN total_price DECIMAL(10,2) NULL AFTER parcel_photo_url,
ADD COLUMN refund_amount DECIMAL(10,2) NULL AFTER total_price;

-- Ajouter le champ pour la raison d'annulation
ALTER TABLE booking 
ADD COLUMN cancellation_reason TEXT NULL AFTER refund_amount;

-- Créer les index pour les recherches fréquentes
CREATE INDEX idx_booking_status ON booking(status);
CREATE INDEX idx_booking_payment_deadline ON booking(payment_deadline);
CREATE INDEX idx_booking_confirmed_at ON booking(confirmed_at);
CREATE INDEX idx_booking_delivered_at ON booking(delivered_at);

-- Migrer les données existantes vers le nouveau système de statut
-- On essaie de mapper les anciens statuts numériques vers les nouveaux statuts enum
UPDATE booking 
SET status = CASE 
    WHEN cancelled = 1 THEN 'CANCELLED_BY_CLIENT'
    WHEN payment_status = 1 AND expedition_status = 0 THEN 'CONFIRMED_PAID'
    WHEN payment_status = 0 AND expedition_status = 0 THEN 'PENDING_CONFIRMATION'
    WHEN expedition_status = 1 THEN 'IN_TRANSIT'
    WHEN customer_reception_status = 1 OR sender_reception_status = 1 THEN 'CONFIRMED_BY_RECEIVER'
    ELSE 'PENDING_CONFIRMATION'
END
WHERE status IS NULL;

-- Rendre le status NOT NULL après la migration
ALTER TABLE booking 
MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING_CONFIRMATION';

-- Commentaires
ALTER TABLE booking 
MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING_CONFIRMATION' 
    COMMENT 'Statut de la réservation (PENDING_CONFIRMATION, CONFIRMED_PAID, etc.)';

ALTER TABLE booking 
MODIFY COLUMN parcel_photo_url VARCHAR(500) NULL 
    COMMENT 'URL de la photo du colis uploadée par le client';

ALTER TABLE booking 
MODIFY COLUMN cancellation_reason TEXT NULL 
    COMMENT 'Raison de l\'annulation de la réservation';

-- Note: Les anciens champs (payment_status, expedition_status, etc.) sont conservés
-- pour compatibilité backward. Ils pourront être supprimés dans une future migration.
