-- V13__Increase_Booking_Status_Column_Length.sql
-- Augmenter la taille de la colonne status pour accommoder les nouveaux statuts de livraison

-- Les nouveaux statuts sont plus longs (ex: PARCEL_DELIVERED_TO_RECEIVER = 29 caractères)
ALTER TABLE booking MODIFY COLUMN status VARCHAR(50);
