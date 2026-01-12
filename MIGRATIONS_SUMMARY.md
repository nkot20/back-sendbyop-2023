# Migrations SQL - Sprint 1

**Date:** 23 octobre 2025  
**Versions:** V4 à V8  
**Base de données:** MySQL/MariaDB

---

## 📋 Migrations Créées

### V4__Alter_Receiver_Table.sql
**Objectif:** Améliorer la table `receiver`

**Modifications:**
- Renommer `phone` → `phone_number`
- Ajouter `address`, `city`, `country`
- Ajouter `status` (ACTIVE par défaut)
- Ajouter `created_at`, `updated_at`
- Ajouter contraintes UNIQUE sur `phone_number` et `email`
- Créer index sur `status`, `city`, `country`

**Impact:** Aucune perte de données, ajout de colonnes NULL sauf status

---

### V5__Alter_Booking_Add_Status_And_Fields.sql
**Objectif:** Moderniser la table `booking` avec système de statut enum

**Modifications:**
- Ajouter `status` (enum VARCHAR) avec valeur par défaut 'PENDING_CONFIRMATION'
- Ajouter timestamps: `confirmed_at`, `paid_at`, `payment_deadline`, `delivered_at`, `cancelled_at`
- Ajouter `parcel_photo_url` (photo du colis)
- Ajouter `total_price`, `refund_amount` (DECIMAL)
- Ajouter `cancellation_reason` (TEXT)
- Créer index sur `status`, `payment_deadline`, `confirmed_at`, `delivered_at`
- **Migration de données:** Mapper anciens statuts numériques → nouveaux statuts enum

**Mapping statuts:**
```sql
cancelled = 1                                    → CANCELLED_BY_CLIENT
payment_status = 1 AND expedition_status = 0    → CONFIRMED_PAID
payment_status = 0 AND expedition_status = 0    → PENDING_CONFIRMATION
expedition_status = 1                           → IN_TRANSIT
customer/sender_reception_status = 1            → CONFIRMED_BY_RECEIVER
default                                         → PENDING_CONFIRMATION
```

**Impact:** 
- Compatibilité backward préservée (anciens champs conservés)
- Migration automatique des statuts existants

---

### V6__Create_Platform_Settings_Table.sql
**Objectif:** Créer table de configuration de la plateforme

**Structure:**
```
platform_settings
├── id (PK)
├── min_price_per_kg (DECIMAL 10,2) DEFAULT 5.00
├── max_price_per_kg (DECIMAL 10,2) DEFAULT 50.00
├── traveler_percentage (DECIMAL 5,2) DEFAULT 70.00
├── platform_percentage (DECIMAL 5,2) DEFAULT 25.00
├── vat_percentage (DECIMAL 5,2) DEFAULT 5.00
├── payment_timeout_hours (INT) DEFAULT 12
├── auto_payout_delay_hours (INT) DEFAULT 24
├── cancellation_deadline_hours (INT) DEFAULT 24
├── late_cancellation_penalty (DECIMAL 5,2) DEFAULT 0.50
├── updated_at (DATETIME)
└── updated_by (VARCHAR 100)
```

**Contraintes:**
- `min_price_per_kg < max_price_per_kg`
- `sum(percentages) = 100`
- `payment_timeout_hours BETWEEN 2 AND 24`
- `auto_payout_delay_hours BETWEEN 12 AND 72`
- `cancellation_deadline_hours BETWEEN 12 AND 72`
- `late_cancellation_penalty BETWEEN 0 AND 1`

**Données initiales:**
- 1 ligne insérée avec les valeurs par défaut
- Prête à l'emploi dès la migration

**Impact:** Nouvelle table, pas d'impact sur existant

---

### V7__Create_Notification_Log_Table.sql
**Objectif:** Créer log de toutes les notifications

**Structure:**
```
notification_log
├── id (BIGINT, PK)
├── type (VARCHAR 50) - Type de notification
├── booking_id (INT, FK) - Lien vers réservation
├── recipient_email (VARCHAR 255)
├── recipient_name (VARCHAR 255)
├── subject (VARCHAR 500)
├── content (TEXT)
├── sent (BOOLEAN) DEFAULT FALSE
├── sent_at (DATETIME)
├── error_message (TEXT)
├── retry_count (INT) DEFAULT 0
└── created_at (DATETIME)
```

**Clés étrangères:**
- `booking_id` → `booking.id` (ON DELETE CASCADE)

**Index:**
- `booking_id`, `type`, `sent_at`, `sent`, `recipient_email`, `created_at`

**Impact:** 
- Nouvelle table, pas d'impact sur existant
- Traçabilité complète de toutes les notifications dès activation

---

### V8__Create_Payout_Table.sql
**Objectif:** Créer table des versements aux voyageurs

**Structure:**
```
payout
├── id (BIGINT, PK)
├── booking_id (INT, UNIQUE, FK) - OneToOne
├── traveler_id (INT, FK)
├── total_amount (DECIMAL 10,2)
├── traveler_amount (DECIMAL 10,2)
├── platform_amount (DECIMAL 10,2)
├── vat_amount (DECIMAL 10,2)
├── traveler_percentage (DECIMAL 5,2)
├── platform_percentage (DECIMAL 5,2)
├── vat_percentage (DECIMAL 5,2)
├── status (VARCHAR 20) DEFAULT 'PENDING'
├── transaction_id (VARCHAR 255)
├── payment_method (VARCHAR 50)
├── error_message (TEXT)
├── created_at (DATETIME)
├── completed_at (DATETIME)
└── cancelled_at (DATETIME)
```

**Clés étrangères:**
- `booking_id` → `booking.id` (ON DELETE RESTRICT)
- `traveler_id` → `customer.id` (ON DELETE RESTRICT)

**Contraintes:**
- `traveler_amount + platform_amount + vat_amount = total_amount`
- `sum(percentages) = 100`

**Index:**
- `booking_id`, `traveler_id`, `status`, `created_at`, `completed_at`, `transaction_id`

**Impact:** 
- Nouvelle table, pas d'impact sur existant
- Relation OneToOne avec booking (1 booking = 1 payout max)

---

## 🔄 Ordre d'Exécution

Les migrations s'exécutent automatiquement dans l'ordre avec Flyway:

1. **V4** - Modifier `receiver`
2. **V5** - Modifier `booking` + migrer données
3. **V6** - Créer `platform_settings` + insérer données
4. **V7** - Créer `notification_log`
5. **V8** - Créer `payout`

---

## ⚠️ Points d'Attention

### Compatibilité Backward

**Table `booking`:**
- ✅ Anciens champs conservés (`payment_status`, `expedition_status`, etc.)
- ✅ Migration automatique vers nouveau système de statut
- ✅ Pas de perte de données
- ⚠️ Les anciens champs pourront être supprimés dans une future migration V9

### Contraintes de Clés Étrangères

**`notification_log.booking_id`:**
- `ON DELETE CASCADE` - Si booking supprimé, logs supprimés aussi

**`payout.booking_id` et `payout.traveler_id`:**
- `ON DELETE RESTRICT` - Empêche suppression booking/customer si payout existe
- Protection des données financières

### Données Initiales

**`platform_settings`:**
- 1 ligne insérée automatiquement
- Modifiable via interface admin
- Ne pas supprimer cette ligne (app en dépend)

---

## 🧪 Vérification Post-Migration

### 1. Vérifier les tables créées
```sql
SHOW TABLES LIKE '%receiver%';
SHOW TABLES LIKE '%booking%';
SHOW TABLES LIKE '%platform_settings%';
SHOW TABLES LIKE '%notification_log%';
SHOW TABLES LIKE '%payout%';
```

### 2. Vérifier les colonnes ajoutées
```sql
DESCRIBE receiver;
DESCRIBE booking;
```

### 3. Vérifier les contraintes
```sql
SHOW CREATE TABLE receiver;
SHOW CREATE TABLE booking;
SHOW CREATE TABLE payout;
```

### 4. Vérifier les données initiales
```sql
SELECT * FROM platform_settings;
```

### 5. Vérifier la migration des statuts
```sql
SELECT 
    COUNT(*) as total,
    status,
    COUNT(*) * 100.0 / SUM(COUNT(*)) OVER() as percentage
FROM booking
GROUP BY status;
```

---

## 🔧 Rollback (Si Nécessaire)

### Annuler V8 (payout)
```sql
DROP TABLE IF EXISTS payout;
DELETE FROM flyway_schema_history WHERE version = '8';
```

### Annuler V7 (notification_log)
```sql
DROP TABLE IF EXISTS notification_log;
DELETE FROM flyway_schema_history WHERE version = '7';
```

### Annuler V6 (platform_settings)
```sql
DROP TABLE IF EXISTS platform_settings;
DELETE FROM flyway_schema_history WHERE version = '6';
```

### Annuler V5 (booking)
```sql
ALTER TABLE booking 
DROP COLUMN status,
DROP COLUMN confirmed_at,
DROP COLUMN paid_at,
DROP COLUMN payment_deadline,
DROP COLUMN delivered_at,
DROP COLUMN cancelled_at,
DROP COLUMN parcel_photo_url,
DROP COLUMN total_price,
DROP COLUMN refund_amount,
DROP COLUMN cancellation_reason;

DROP INDEX idx_booking_status ON booking;
DROP INDEX idx_booking_payment_deadline ON booking;
DROP INDEX idx_booking_confirmed_at ON booking;
DROP INDEX idx_booking_delivered_at ON booking;

DELETE FROM flyway_schema_history WHERE version = '5';
```

### Annuler V4 (receiver)
```sql
ALTER TABLE receiver 
CHANGE COLUMN phone_number phone VARCHAR(255),
DROP COLUMN address,
DROP COLUMN city,
DROP COLUMN country,
DROP COLUMN status,
DROP COLUMN created_at,
DROP COLUMN updated_at;

DROP INDEX idx_receiver_status ON receiver;
DROP INDEX idx_receiver_city ON receiver;
DROP INDEX idx_receiver_country ON receiver;

ALTER TABLE receiver DROP CONSTRAINT uk_receiver_phone;

DELETE FROM flyway_schema_history WHERE version = '4';
```

---

## 📊 Impact sur la Base de Données

### Tables Modifiées
- `receiver` - 7 colonnes ajoutées, 1 renommée
- `booking` - 10 colonnes ajoutées

### Tables Créées
- `platform_settings` - 12 colonnes
- `notification_log` - 11 colonnes
- `payout` - 17 colonnes

### Index Créés
- 19 nouveaux index au total

### Contraintes Ajoutées
- 2 UNIQUE (receiver)
- 6 CHECK (platform_settings)
- 2 CHECK (payout)
- 2 FK (notification_log)
- 2 FK (payout)

---

## ✅ Checklist de Migration

- [ ] Backup de la base de données effectué
- [ ] Migrations V4-V8 présentes dans `db/migration/`
- [ ] Application arrêtée
- [ ] Migration exécutée: `.\mvnw.cmd flyway:migrate`
- [ ] Vérification post-migration OK
- [ ] `platform_settings` contient 1 ligne
- [ ] Statuts `booking` migrés correctement
- [ ] Application redémarrée
- [ ] Tests fonctionnels OK

---

**Migrations prêtes à être appliquées ! 🚀**

**Commande:**
```bash
.\mvnw.cmd flyway:migrate
```
