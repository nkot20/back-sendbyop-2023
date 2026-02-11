-- Migration V18: Création des tables KYC (Know Your Customer)

-- Table pour stocker les documents KYC
CREATE TABLE IF NOT EXISTS kyc_document (
    id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(100),
    front_image_path VARCHAR(255),
    back_image_path VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    rejection_reason TEXT,
    submitted_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    expiry_date DATE,
    country_of_issue VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kyc_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_kyc_customer ON kyc_document(customer_id);
CREATE INDEX idx_kyc_status ON kyc_document(status);
CREATE INDEX idx_kyc_submitted_at ON kyc_document(submitted_at DESC);
CREATE INDEX idx_kyc_expiry ON kyc_document(expiry_date);

-- Ajouter un commentaire sur la table
COMMENT ON TABLE kyc_document IS 'Stocke les documents d''identité soumis par les clients pour vérification KYC';
COMMENT ON COLUMN kyc_document.status IS 'Statuts possibles: NOT_SUBMITTED, PENDING_REVIEW, APPROVED, REJECTED, EXPIRED';
COMMENT ON COLUMN kyc_document.document_type IS 'Types: PASSPORT, ID_CARD, DRIVER_LICENSE';
