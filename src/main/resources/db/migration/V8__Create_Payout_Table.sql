-- ============================================
-- Migration: Création de la table payout
-- Version: V8
-- Date: 2025-10-23
-- Description: Gestion des versements aux voyageurs
-- ============================================

CREATE TABLE payout (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Relations
    booking_id INT NOT NULL UNIQUE 
        COMMENT 'Réservation associée (relation OneToOne)',
    traveler_id INT NOT NULL 
        COMMENT 'Voyageur recevant le versement',
    
    -- Montants
    total_amount DECIMAL(10,2) NOT NULL 
        COMMENT 'Montant total de la réservation',
    traveler_amount DECIMAL(10,2) NOT NULL 
        COMMENT 'Montant revenant au voyageur',
    platform_amount DECIMAL(10,2) NOT NULL 
        COMMENT 'Montant revenant à la plateforme',
    vat_amount DECIMAL(10,2) NOT NULL 
        COMMENT 'Montant de la TVA',
    
    -- Pourcentages appliqués (pour historique)
    traveler_percentage DECIMAL(5,2) NOT NULL 
        COMMENT 'Pourcentage appliqué au voyageur',
    platform_percentage DECIMAL(5,2) NOT NULL 
        COMMENT 'Pourcentage appliqué à la plateforme',
    vat_percentage DECIMAL(5,2) NOT NULL 
        COMMENT 'Pourcentage de TVA appliqué',
    
    -- Statut
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        COMMENT 'Statut du versement (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED)',
    
    -- Informations de paiement
    transaction_id VARCHAR(255) NULL 
        COMMENT 'ID de transaction du système de paiement',
    payment_method VARCHAR(50) NULL 
        COMMENT 'Méthode de paiement utilisée',
    
    -- Erreurs
    error_message TEXT NULL 
        COMMENT 'Message d\'erreur en cas d\'échec',
    
    -- Dates
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
        COMMENT 'Date de création du versement',
    completed_at DATETIME NULL 
        COMMENT 'Date de complétion du versement',
    cancelled_at DATETIME NULL 
        COMMENT 'Date d\'annulation du versement',
    
    -- Clés étrangères
    CONSTRAINT fk_payout_booking 
        FOREIGN KEY (booking_id) 
        REFERENCES booking(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT fk_payout_traveler 
        FOREIGN KEY (traveler_id) 
        REFERENCES customer(id) 
        ON DELETE RESTRICT,
    
    -- Contraintes de validation
    CONSTRAINT chk_payout_amounts CHECK (
        traveler_amount + platform_amount + vat_amount = total_amount
    ),
    
    CONSTRAINT chk_payout_percentages CHECK (
        traveler_percentage + platform_percentage + vat_percentage = 100.00
    ),
    
    -- Index pour recherches fréquentes
    INDEX idx_payout_booking (booking_id),
    INDEX idx_payout_traveler (traveler_id),
    INDEX idx_payout_status (status),
    INDEX idx_payout_created_at (created_at),
    INDEX idx_payout_completed_at (completed_at),
    INDEX idx_payout_transaction (transaction_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Gestion des versements aux voyageurs après livraison confirmée';

-- Commentaire sur la table
ALTER TABLE payout COMMENT = 
'Gère les versements aux voyageurs. Chaque réservation payée génère un payout qui sera '
'traité soit après confirmation du destinataire, soit automatiquement après 24h. '
'La répartition est historisée pour traçabilité.';
