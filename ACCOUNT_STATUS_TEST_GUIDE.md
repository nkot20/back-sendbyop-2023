# Guide de Test - Système de Statuts de Compte

## ✅ Modifications Effectuées

### 1️⃣ `UserRegistrationService.java` - Vérification de la sauvegarde

**Changement:**
```java
// AVANT: Pas de vérification
userService.saveUser(User.builder()...build());

// APRÈS: Vérification + log
User savedUser = userService.saveUser(User.builder()...build());
if (savedCustomer == null || savedUser == null) {
    throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Failed to create customer or user account");
}
log.info("Customer and User account created successfully for email: {}", customer.getEmail());
```

### 2️⃣ `AuthServiceImpl.java` - Vérification du statut au login

**Changement:**
```java
@Override
public JwtResponse authenticateUser(@Valid LoginForm loginRequest) throws SendByOpException {
    // 1. Authentifier
    Authentication authentication = authenticationManager.authenticate(...);
    
    // 2. Récupérer le User et vérifier son statut
    User user = userService.findByEmail(loginRequest.getUsername());
    
    // 3. Vérifier le statut
    if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
        log.warn("Login attempt with unverified email: {}", ...);
        throw new SendByOpException(ErrorInfo.EMAIL_NOT_VERIFIED);
    }
    
    if (user.getStatus() == AccountStatus.BLOCKED) {
        throw new SendByOpException(ErrorInfo.ACCOUNT_BLOCKED);
    }
    
    if (user.getStatus() == AccountStatus.INACTIVE) {
        throw new SendByOpException(ErrorInfo.ACCOUNT_INACTIVE);
    }
    
    // 4. Générer JWT si ACTIVE
    String jwt = jwtProvider.generateJwtToken(authentication);
    return new JwtResponse(...);
}
```

---

## 🧪 Tests à Effectuer

### Test 1: Vérifier que le User est bien sauvegardé

**1. Lancer l'application**
```powershell
.\mvnw.cmd spring-boot:run
```

**2. Inscription d'un nouveau client**
```bash
POST http://localhost:9002/auth/register
Content-Type: application/json

{
  "firstName": "Test",
  "lastName": "User",
  "email": "test@example.com",
  "password": "SecurePass123!",
  "phoneNumber": "+33612345678",
  "country": "France",
  "address": "123 Rue Test"
}
```

**3. Vérifier les logs**
Recherchez dans les logs:
```
Customer and User account created successfully for email: test@example.com
```

**4. Vérifier en base de données**
```sql
-- Vérifier que le Customer existe
SELECT * FROM customer WHERE email = 'test@example.com';

-- Vérifier que le User existe avec le bon statut
SELECT id, email, username, status FROM user WHERE email = 'test@example.com';
-- Résultat attendu: status = 'PENDING_VERIFICATION'
```

---

### Test 2: Login AVANT vérification d'email (doit être refusé)

**1. Tenter de se connecter**
```bash
POST http://localhost:9002/auth/login
Content-Type: application/json

{
  "username": "test@example.com",
  "password": "SecurePass123!"
}
```

**2. Réponse attendue (403 Forbidden)**
```json
{
  "timestamp": "2025-10-21T23:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "EMAIL_NOT_VERIFIED: Email not verified. Please check your inbox for verification link",
  "path": "/auth/login"
}
```

**3. Vérifier les logs**
```
Login attempt for username: test@example.com
Login attempt with unverified email: test@example.com
```

---

### Test 3: Vérification d'email

**1. Récupérer le token de vérification en base**
```sql
SELECT token FROM verify_token WHERE email = 'test@example.com' ORDER BY id DESC LIMIT 1;
```

**2. Vérifier l'email**
```bash
POST http://localhost:9002/customer/verify/email/{TOKEN}
```

**3. Réponse attendue (200 OK)**
```json
{
  "message": "Email verified successfully"
}
```

**4. Vérifier en base que le statut a changé**
```sql
SELECT id, email, status FROM user WHERE email = 'test@example.com';
-- Résultat attendu: status = 'ACTIVE'

SELECT email_verified FROM customer WHERE email = 'test@example.com';
-- Résultat attendu: email_verified = 1
```

---

### Test 4: Login APRÈS vérification (doit être autorisé)

**1. Tenter de se connecter**
```bash
POST http://localhost:9002/auth/login
Content-Type: application/json

{
  "username": "test@example.com",
  "password": "SecurePass123!"
}
```

**2. Réponse attendue (200 OK)**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "test@example.com",
  "authorities": ["ROLE_CUSTOMER"]
}
```

**3. Vérifier les logs**
```
Login attempt for username: test@example.com
Login successful for user: test@example.com with status: ACTIVE
```

---

### Test 5: Bloquer un compte (Admin)

**1. Bloquer manuellement en base**
```sql
UPDATE user SET status = 'BLOCKED' WHERE email = 'test@example.com';
```

**2. Tenter de se connecter**
```bash
POST http://localhost:9002/auth/login
Content-Type: application/json

{
  "username": "test@example.com",
  "password": "SecurePass123!"
}
```

**3. Réponse attendue (403 Forbidden)**
```json
{
  "timestamp": "2025-10-21T23:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "ACCOUNT_BLOCKED: Account has been blocked. Please contact support",
  "path": "/auth/login"
}
```

**4. Vérifier les logs**
```
Login attempt for username: test@example.com
Login attempt with blocked account: test@example.com
```

---

## 🔍 Diagnostics

### Problème: Le User n'est pas sauvegardé

**Vérification:**
```sql
-- Compter les Users
SELECT COUNT(*) FROM user;

-- Compter les Customers
SELECT COUNT(*) FROM customer;

-- Voir les derniers enregistrements
SELECT * FROM user ORDER BY id DESC LIMIT 5;
SELECT * FROM customer ORDER BY id DESC LIMIT 5;
```

**Causes possibles:**
1. **Transaction rollback** à cause de l'email qui échoue
2. **Contrainte unique violée** (email déjà existant)
3. **Champ status manquant** en base

**Solution si status manquant:**
```sql
-- Ajouter la colonne status
ALTER TABLE user 
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION';

-- Mettre les comptes existants à ACTIVE
UPDATE user SET status = 'ACTIVE' WHERE status IS NULL OR status = '';

-- Index
CREATE INDEX idx_user_status ON user(status);
```

---

### Problème: Login autorisé malgré statut PENDING_VERIFICATION

**Cause:** Code d'`AuthServiceImpl` pas déployé

**Solution:**
1. Redémarrer l'application
2. Vérifier les logs au démarrage
3. Tester à nouveau

---

### Problème: Erreur "status cannot be null"

**Cause:** Colonne `status` pas encore ajoutée en base

**Solution:**
```sql
-- Vérifier si la colonne existe
DESCRIBE user;

-- Si elle n'existe pas, l'ajouter
ALTER TABLE user 
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION';
```

---

## 📊 Matrice de Tests

| Scénario | User.status | Login Autorisé | Code Retour | Message |
|----------|-------------|----------------|-------------|---------|
| Nouveau compte | `PENDING_VERIFICATION` | ❌ Non | 403 | EMAIL_NOT_VERIFIED |
| Email vérifié | `ACTIVE` | ✅ Oui | 200 | JWT Token |
| Compte bloqué | `BLOCKED` | ❌ Non | 403 | ACCOUNT_BLOCKED |
| Compte inactif | `INACTIVE` | ❌ Non | 403 | ACCOUNT_INACTIVE |

---

## 🚀 Checklist Finale

### Base de Données
- [ ] Colonne `status` ajoutée à la table `user`
- [ ] Index `idx_user_status` créé
- [ ] Comptes existants mis à `ACTIVE`

### Code
- [ ] `UserRegistrationService` vérifie que User est sauvegardé
- [ ] `AuthServiceImpl` vérifie le statut au login
- [ ] Application redémarrée

### Tests
- [ ] Inscription crée bien un User avec `PENDING_VERIFICATION`
- [ ] Login refusé si `PENDING_VERIFICATION`
- [ ] Vérification email change statut à `ACTIVE`
- [ ] Login autorisé si `ACTIVE`
- [ ] Login refusé si `BLOCKED` ou `INACTIVE`

---

## 📝 Commandes Utiles

### Vérifier les statuts actuels
```sql
SELECT 
    u.id,
    u.email,
    u.status,
    c.email_verified,
    c.phone_verified
FROM user u
LEFT JOIN customer c ON u.email = c.email
ORDER BY u.id DESC
LIMIT 10;
```

### Réinitialiser un compte pour test
```sql
-- Remettre à PENDING_VERIFICATION
UPDATE user SET status = 'PENDING_VERIFICATION' WHERE email = 'test@example.com';
UPDATE customer SET email_verified = 0 WHERE email = 'test@example.com';

-- Supprimer les anciens tokens
DELETE FROM verify_token WHERE email = 'test@example.com';
```

### Voir les logs en temps réel (Windows PowerShell)
```powershell
Get-Content -Path "logs/spring-boot-application.log" -Wait -Tail 50
```

---

**Date:** 21 octobre 2025  
**Fichiers modifiés:**
- `UserRegistrationService.java`
- `AuthServiceImpl.java`

**À tester:** Tous les scénarios ci-dessus avant déploiement en production
