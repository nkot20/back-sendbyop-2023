-- Migration V19: Correction de la contrainte flight_status_check
-- Ajoute les nouveaux statuts FlightStatus enum

-- Supprimer l'ancienne contrainte si elle existe
ALTER TABLE flight DROP CONSTRAINT IF EXISTS flight_status_check;

-- Ajouter la nouvelle contrainte avec tous les statuts possibles
ALTER TABLE flight ADD CONSTRAINT flight_status_check 
CHECK (status IN ('PENDING_VALIDATION', 'ACTIVE', 'REJECTED', 'EXPIRED', 'CANCELLED'));

-- Commentaire sur la colonne
COMMENT ON COLUMN flight.status IS 'Statuts possibles: PENDING_VALIDATION, ACTIVE, REJECTED, EXPIRED, CANCELLED';
