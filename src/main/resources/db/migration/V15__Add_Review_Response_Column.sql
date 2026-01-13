-- Ajoute une colonne pour permettre au voyageur de répondre aux avis

ALTER TABLE review ADD COLUMN IF NOT EXISTS response TEXT DEFAULT NULL 
    COMMENT 'Réponse du voyageur à l''avis reçu';

ALTER TABLE review ADD COLUMN IF NOT EXISTS response_date DATETIME DEFAULT NULL 
    COMMENT 'Date de la réponse';
