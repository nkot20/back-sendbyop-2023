-- Migration: Ajout du champ status à la table User
-- Date: 2025-10-21
-- Description: Ajoute le statut du compte (PENDING_VERIFICATION, ACTIVE, BLOCKED, INACTIVE)

-- Ajout de la colonne status
ALTER TABLE user 
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION';

-- Mettre tous les comptes existants à ACTIVE (ils sont déjà vérifiés)
UPDATE user 
SET status = 'ACTIVE';

-- Créer un index sur status pour améliorer les performances des requêtes
CREATE INDEX idx_user_status ON user(status);

-- Commentaires
COMMENT ON COLUMN user.status IS 'Statut du compte: PENDING_VERIFICATION, ACTIVE, BLOCKED, INACTIVE';
