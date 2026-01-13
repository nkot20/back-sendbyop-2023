-- V11__Create_Transactions_Table.sql
-- Création de la table des transactions de paiement

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(100) UNIQUE NOT NULL COMMENT 'Référence unique de la transaction',
    external_transaction_id VARCHAR(255) COMMENT 'ID de la transaction chez le provider',
    booking_id INT NOT NULL COMMENT 'Référence à la réservation',
    customer_id INT NOT NULL COMMENT 'Référence au client',
    amount DECIMAL(10, 2) NOT NULL COMMENT 'Montant du paiement',
    currency VARCHAR(10) NOT NULL DEFAULT 'XAF' COMMENT 'Devise (XAF = Franc CFA)',
    payment_method VARCHAR(50) NOT NULL COMMENT 'Méthode de paiement',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT 'Statut de la transaction',
    phone_number VARCHAR(20) COMMENT 'Numéro de téléphone pour Orange/MTN',
    payment_details TEXT COMMENT 'Détails supplémentaires du paiement en JSON',
    error_message TEXT COMMENT 'Message d''erreur en cas d''échec',
    error_code VARCHAR(50) COMMENT 'Code d''erreur',
    created_at DATETIME NOT NULL COMMENT 'Date de création',
    updated_at DATETIME COMMENT 'Date de dernière mise à jour',
    completed_at DATETIME COMMENT 'Date de completion du paiement',
    webhook_received_at DATETIME COMMENT 'Date de réception du webhook',
    invoice_url VARCHAR(500) COMMENT 'URL de la facture PDF',
    invoice_sent TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Facture envoyée ou non',
    retry_count INT NOT NULL DEFAULT 0 COMMENT 'Nombre de tentatives',
    idempotency_key VARCHAR(100) UNIQUE COMMENT 'Clé d''idempotence pour éviter les doublons',
    
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE RESTRICT,
    
    INDEX idx_transaction_reference (transaction_reference),
    INDEX idx_booking_id (booking_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_payment_method (payment_method),
    INDEX idx_created_at (created_at),
    INDEX idx_idempotency_key (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Table des transactions de paiement';
