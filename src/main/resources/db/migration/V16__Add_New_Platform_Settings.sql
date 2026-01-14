-- Ajout des nouveaux paramètres de configuration de la plateforme

-- Commission plateforme (15%)
ALTER TABLE platform_settings 
ADD COLUMN commission_percentage DECIMAL(5,2) NOT NULL DEFAULT 15.00 
COMMENT 'Commission plateforme en pourcentage (15% par défaut)';

-- Délai de confirmation de réception (72h)
ALTER TABLE platform_settings 
ADD COLUMN reception_confirmation_hours INT NOT NULL DEFAULT 72 
COMMENT 'Délai en heures pour confirmer la réception (72h par défaut)';

-- Délai pour donner un avis (90 jours)
ALTER TABLE platform_settings 
ADD COLUMN review_deadline_days INT NOT NULL DEFAULT 90 
COMMENT 'Délai en jours pour laisser un avis après livraison';

-- Seuil minimum de reversement (50 EUR)
ALTER TABLE platform_settings 
ADD COLUMN minimum_payout_amount DECIMAL(10,2) NOT NULL DEFAULT 50.00 
COMMENT 'Montant minimum pour demander un virement (50 EUR par défaut)';

-- Frais de virement couverts par SendByOp (5 EUR)
ALTER TABLE platform_settings 
ADD COLUMN transfer_fee_covered DECIMAL(10,2) NOT NULL DEFAULT 5.00 
COMMENT 'Frais de virement couverts par SendByOp (5 EUR par défaut)';

-- Taux de remboursement avant 4h du vol (90%)
ALTER TABLE platform_settings 
ADD COLUMN refund_rate_before_deadline DECIMAL(5,2) NOT NULL DEFAULT 90.00 
COMMENT 'Taux de remboursement si annulation avant 4h du vol (90%)';

-- Délai critique avant le vol (4h)
ALTER TABLE platform_settings 
ADD COLUMN critical_cancellation_hours INT NOT NULL DEFAULT 4 
COMMENT 'Délai critique avant vol pour annulation (4h par défaut)';

-- TVA Europe (20%)
ALTER TABLE platform_settings 
ADD COLUMN vat_rate_europe DECIMAL(5,2) NOT NULL DEFAULT 20.00 
COMMENT 'Taux de TVA en Europe (20%)';

-- Montant d'assurance fixe
ALTER TABLE platform_settings 
ADD COLUMN insurance_amount DECIMAL(10,2) NOT NULL DEFAULT 5.00 
COMMENT 'Montant fixe de l''assurance (5 EUR par défaut)';

-- Ajout d'un statut pour les vols
ALTER TABLE flight 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
COMMENT 'Statut du vol: ACTIVE, EXPIRED, CANCELLED'
AFTER cancelled;

-- Index sur le statut du vol pour optimiser les recherches
CREATE INDEX idx_flight_status ON flight(status);
CREATE INDEX idx_flight_arrival_date ON flight(arrival_date);
