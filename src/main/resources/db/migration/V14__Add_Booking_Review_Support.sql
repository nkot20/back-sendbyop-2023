-- Migration pour supporter les avis sur les réservations
-- Ajoute une relation optionnelle entre Review et Booking

-- Ajouter la colonne booking_id (optionnelle, permet les anciens avis sans booking)
ALTER TABLE review ADD COLUMN IF NOT EXISTS booking_id INT DEFAULT NULL 
    COMMENT 'ID de la réservation concernée par l''avis (optionnel pour compatibilité)';

-- Ajouter une contrainte de clé étrangère
ALTER TABLE review ADD CONSTRAINT IF NOT EXISTS fk_review_booking 
    FOREIGN KEY (booking_id) REFERENCES booking(id) 
    ON DELETE CASCADE;

-- Modifier le type de rating de VARCHAR à INT pour faciliter les notes de 1 à 5
-- (Commenté car cela pourrait casser les données existantes)
-- ALTER TABLE review MODIFY COLUMN rating INT DEFAULT NULL COMMENT 'Note de 1 à 5';

-- Créer un index pour améliorer les performances des requêtes
CREATE INDEX IF NOT EXISTS idx_review_booking_id ON review (booking_id);
CREATE INDEX IF NOT EXISTS idx_review_transporter_id ON review (transporter_id);
