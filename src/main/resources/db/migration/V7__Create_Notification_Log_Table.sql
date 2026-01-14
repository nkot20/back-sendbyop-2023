-- ============================================
-- Migration: Création de la table notification_log
-- Version: V7
-- Date: 2025-10-23
-- Description: Log de toutes les notifications envoyées
-- ============================================

CREATE TABLE notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Type de notification
    type VARCHAR(50) NOT NULL 
        COMMENT 'Type de notification (BOOKING_CREATED, PAYMENT_RECEIVED, etc.)',
    
    -- Réservation associée
    booking_id INT NOT NULL,
    
    -- Destinataire
    recipient_email VARCHAR(255) NOT NULL 
        COMMENT 'Email du destinataire de la notification',
    recipient_name VARCHAR(255) NULL 
        COMMENT 'Nom du destinataire',
    
    -- Contenu
    subject VARCHAR(500) NOT NULL 
        COMMENT 'Sujet de l\'email',
    content TEXT NULL 
        COMMENT 'Contenu de l\'email (peut être volumineux)',
    
    -- Statut d'envoi
    sent BOOLEAN NOT NULL DEFAULT FALSE 
        COMMENT 'Indique si l\'email a été envoyé avec succès',
    sent_at DATETIME NULL 
        COMMENT 'Date et heure d\'envoi réussi',
    
    -- Erreurs
    error_message TEXT NULL 
        COMMENT 'Message d\'erreur si l\'envoi a échoué',
    retry_count INT NOT NULL DEFAULT 0 
        COMMENT 'Nombre de tentatives d\'envoi',
    
    -- Audit
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
        COMMENT 'Date de création de l\'entrée de log',
    
    -- Clés étrangères
    CONSTRAINT fk_notification_booking 
        FOREIGN KEY (booking_id) 
        REFERENCES booking(id) 
        ON DELETE CASCADE,
    
    -- Index pour recherches fréquentes
    INDEX idx_notification_booking (booking_id),
    INDEX idx_notification_type (type),
    INDEX idx_notification_sent_at (sent_at),
    INDEX idx_notification_sent (sent),
    INDEX idx_notification_recipient_email (recipient_email),
    INDEX idx_notification_created_at (created_at)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Log de toutes les notifications email envoyées';

-- Commentaire sur la table
ALTER TABLE notification_log COMMENT = 
'Traçabilité complète de toutes les notifications envoyées dans le système. '
'Permet de savoir qui a reçu quoi et quand, avec gestion des erreurs et des retries.';
