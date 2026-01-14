-- ============================================
-- Migration: Création de la table platform_settings
-- Version: V6
-- Date: 2025-10-23
-- Description: Paramètres de configuration de la plateforme
-- ============================================

CREATE TABLE platform_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Tarifs
    min_price_per_kg DECIMAL(10,2) NOT NULL DEFAULT 5.00 
        COMMENT 'Prix minimum par kilogramme',
    max_price_per_kg DECIMAL(10,2) NOT NULL DEFAULT 50.00 
        COMMENT 'Prix maximum par kilogramme',
    
    -- Répartition (en pourcentage)
    traveler_percentage DECIMAL(5,2) NOT NULL DEFAULT 70.00 
        COMMENT 'Pourcentage revenant au voyageur',
    platform_percentage DECIMAL(5,2) NOT NULL DEFAULT 25.00 
        COMMENT 'Pourcentage revenant à la plateforme',
    vat_percentage DECIMAL(5,2) NOT NULL DEFAULT 5.00 
        COMMENT 'Pourcentage de TVA',
    
    -- Délais (en heures)
    payment_timeout_hours INT NOT NULL DEFAULT 12 
        COMMENT 'Délai pour payer après confirmation (2-24h)',
    auto_payout_delay_hours INT NOT NULL DEFAULT 24 
        COMMENT 'Délai avant versement automatique (12-72h)',
    cancellation_deadline_hours INT NOT NULL DEFAULT 24 
        COMMENT 'Délai minimum d\'annulation avant vol (12-72h)',
    
    -- Pénalités
    late_cancellation_penalty DECIMAL(5,2) NOT NULL DEFAULT 0.50 
        COMMENT 'Pénalité d\'annulation tardive (0-1, ex: 0.50 = 50%)',
    
    -- Audit
    updated_at DATETIME NULL,
    updated_by VARCHAR(100) NULL,
    
    -- Contraintes
    CONSTRAINT chk_price_range CHECK (min_price_per_kg < max_price_per_kg),
    CONSTRAINT chk_percentage_sum CHECK (
        traveler_percentage + platform_percentage + vat_percentage = 100.00
    ),
    CONSTRAINT chk_payment_timeout CHECK (
        payment_timeout_hours BETWEEN 2 AND 24
    ),
    CONSTRAINT chk_payout_delay CHECK (
        auto_payout_delay_hours BETWEEN 12 AND 72
    ),
    CONSTRAINT chk_cancellation_deadline CHECK (
        cancellation_deadline_hours BETWEEN 12 AND 72
    ),
    CONSTRAINT chk_penalty_range CHECK (
        late_cancellation_penalty BETWEEN 0 AND 1
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Paramètres de configuration de la plateforme';

-- Insérer les valeurs par défaut
INSERT INTO platform_settings (
    min_price_per_kg,
    max_price_per_kg,
    traveler_percentage,
    platform_percentage,
    vat_percentage,
    payment_timeout_hours,
    auto_payout_delay_hours,
    cancellation_deadline_hours,
    late_cancellation_penalty,
    updated_at
) VALUES (
    5.00,   -- min_price_per_kg
    50.00,  -- max_price_per_kg
    70.00,  -- traveler_percentage
    25.00,  -- platform_percentage
    5.00,   -- vat_percentage
    12,     -- payment_timeout_hours
    24,     -- auto_payout_delay_hours
    24,     -- cancellation_deadline_hours
    0.50,   -- late_cancellation_penalty (50%)
    NOW()   -- updated_at
);
