# Architecture de Gestion des Statuts de Compte

## 🎯 Problématique Résolue

### Avant
- ❌ Vérification d'email stockée dans `Customer.emailVerified`
- ❌ Pas de gestion de statut de compte
- ❌ Impossible de bloquer/désactiver un compte

### Après
- ✅ Statut du compte géré dans `User.status`
- ✅ Vérifications de profil dans `Customer` (email, téléphone, identité)
- ✅ Séparation claire: compte vs profil
- ✅ Possibilité de bloquer/désactiver des comptes

---

## 📊 Nouvelle Architecture

### Table `User` - Statut du Compte

```java
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false)
private AccountStatus status;
```

**Statuts disponibles:**

| Statut | Description | Accès | Actions possibles |
|--------|-------------|-------|-------------------|
| `PENDING_VERIFICATION` | Compte créé, email non vérifié | ❌ Login bloqué | Vérifier email, Renvoyer email |
| `ACTIVE` | Compte actif et vérifié | ✅ Login autorisé | Utiliser la plateforme |
| `BLOCKED` | Compte bloqué (fraude, violation) | ❌ Login bloqué | Contacter support |
| `INACTIVE` | Compte désactivé par l'utilisateur | ❌ Login bloqué | Réactiver le compte |

### Table `Customer` - Vérifications du Profil

```java
@Column(name = "email_verified")
private int emailVerified;  // 0 = non vérifié, 1 = vérifié

@Column(name = "phone_verified")
private int phoneVerified;  // 0 = non vérifié, 1 = vérifié

@Column(name = "identity_verified")
private int identityVerified;  // 0 = non vérifié, 1 = vérifié
```

**Ces champs servent à:**
- Afficher le statut de complétion du profil
- Badge "profil vérifié" dans l'interface
- Débloquer des fonctionnalités premium

---

## 🔄 Flux d'Inscription et Vérification

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUX COMPLET                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Inscription                                              │
│     ├─> Customer créé (emailVerified = 0)                    │
│     ├─> User créé (status = PENDING_VERIFICATION)           │
│     └─> Email de vérification envoyé                         │
│                                                              │
│  2. Tentative de Login AVANT vérification                    │
│     ├─> User.status == PENDING_VERIFICATION                  │
│     ├─> ❌ Login refusé                                      │
│     └─> Email de vérification renvoyé                        │
│                                                              │
│  3. Vérification d'Email                                     │
│     ├─> User.status → ACTIVE                                 │
│     ├─> Customer.emailVerified → 1                           │
│     └─> ✅ Compte activé                                     │
│                                                              │
│  4. Login APRÈS vérification                                 │
│     ├─> User.status == ACTIVE                                │
│     └─> ✅ Login autorisé, JWT généré                        │
│                                                              │
│  5. Vérifications supplémentaires (optionnel)                │
│     ├─> Téléphone vérifié → Customer.phoneVerified = 1      │
│     ├─> Identité vérifiée → Customer.identityVerified = 1   │
│     └─> Badge "Profil Vérifié" affiché                      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 💻 Implémentation Technique

### 1️⃣ Enum `AccountStatus`

**Fichier:** `AccountStatus.java`

```java
public enum AccountStatus {
    PENDING_VERIFICATION,  // En attente de vérification
    ACTIVE,                // Actif
    BLOCKED,               // Bloqué
    INACTIVE               // Inactif
}
```

### 2️⃣ Inscription - Statut Initial

**Fichier:** `UserRegistrationService.java`

```java
public CustomerDto registerNewCustomer(CustomerRegistrationDto registrationDto) {
    // Créer le Customer
    CustomerDto customer = CustomerDto.builder()
        .email(registrationDto.getEmail())
        .emailVerified(0)  // Email non encore vérifié
        .phoneVerified(0)  // Téléphone non encore vérifié
        .build();
    
    CustomerDto savedCustomer = customerService.saveClient(customer);
    
    // Créer le User avec statut PENDING_VERIFICATION
    userService.saveUser(User.builder()
        .email(customer.getEmail())
        .password(passwordEncoder.encode(registrationDto.getPassword()))
        .role(RoleEnum.CUSTOMER.name())
        .status(AccountStatus.PENDING_VERIFICATION)  // ⬅️ Statut initial
        .build());
    
    // Envoyer l'email de vérification
    sendVerificationEmail(savedCustomer);
    
    return savedCustomer;
}
```

### 3️⃣ Vérification d'Email - Activation du Compte

**Fichier:** `UserRegistrationService.java`

```java
public CustomerDto verifyCustomerEmail(String token) {
    String result = verifyTokenService.verifyToken(token);
    
    if (result.equals(AppConstants.TOKEN_VALID)) {
        VerifyToken verifyToken = verifyTokenService.getByTokent(token);
        CustomerDto customer = customerService.getCustomerByEmail(verifyToken.getEmail());
        
        // ✅ Activer le compte User
        User user = userService.findByEmail(verifyToken.getEmail());
        user.setStatus(AccountStatus.ACTIVE);
        userService.updateUser(user);
        
        // ✅ Marquer l'email comme vérifié dans Customer
        customer.setEmailVerified(1);
        return customerService.saveClient(customer);
    }
    
    throw new SendByOpException(ErrorInfo.TOKEN_INVALID);
}
```

### 4️⃣ Login - Vérification du Statut

**Fichier:** `AuthServiceImpl.java`

```java
public JwtResponse authenticateUser(LoginForm loginRequest) {
    // Authentifier avec Spring Security
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(),
            loginRequest.getPassword()
        )
    );
    
    // Récupérer l'utilisateur
    User user = userService.findByEmail(loginRequest.getUsername());
    
    // ⚠️ Vérifier le statut du compte
    if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
        // Renvoyer l'email de vérification
        CustomerDto customer = customerService.getCustomerByEmail(user.getEmail());
        userRegistrationService.resendVerificationEmail(user.getEmail());
        
        throw new SendByOpException(ErrorInfo.EMAIL_NOT_VERIFIED, 
            "Veuillez vérifier votre email. Un nouveau lien vous a été envoyé.");
    }
    
    if (user.getStatus() == AccountStatus.BLOCKED) {
        throw new SendByOpException(ErrorInfo.ACCOUNT_BLOCKED, 
            "Votre compte a été bloqué. Contactez le support.");
    }
    
    if (user.getStatus() == AccountStatus.INACTIVE) {
        throw new SendByOpException(ErrorInfo.ACCOUNT_INACTIVE, 
            "Votre compte est désactivé. Veuillez le réactiver.");
    }
    
    // ✅ Statut ACTIVE → Générer le JWT
    String jwt = jwtProvider.generateJwtToken(authentication);
    String refreshToken = jwtProvider.generateRefreshToken(authentication);
    
    return new JwtResponse(jwt, refreshToken, user.getUsername(), user.getId(), authorities);
}
```

---

## 📱 Réponses API selon le Statut

### Cas 1: Inscription Réussie

**Request:** `POST /auth/register`

**Response (200):**
```json
{
  "message": "Registration successful. Please check your email for verification."
}
```

**État BD:**
- `User.status` = `PENDING_VERIFICATION`
- `Customer.emailVerified` = `0`

---

### Cas 2: Login avec Compte Non Vérifié

**Request:** `POST /auth/login`

**Response (403):**
```json
{
  "timestamp": "2025-10-21T22:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "EMAIL_NOT_VERIFIED: Veuillez vérifier votre email. Un nouveau lien vous a été envoyé.",
  "path": "/auth/login"
}
```

**Actions Backend:**
- Email de vérification renvoyé automatiquement
- Login refusé

---

### Cas 3: Vérification d'Email Réussie

**Request:** `POST /customer/verify/email/{token}`

**Response (200):**
```json
{
  "message": "Email verified successfully"
}
```

**État BD:**
- `User.status` = `ACTIVE`
- `Customer.emailVerified` = `1`

---

### Cas 4: Login avec Compte Actif

**Request:** `POST /auth/login`

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "user@example.com",
  "id": 123,
  "authorities": ["ROLE_CUSTOMER"]
}
```

**État:** Login autorisé

---

### Cas 5: Login avec Compte Bloqué

**Request:** `POST /auth/login`

**Response (403):**
```json
{
  "timestamp": "2025-10-21T22:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "ACCOUNT_BLOCKED: Votre compte a été bloqué. Contactez le support.",
  "path": "/auth/login"
}
```

---

## 🛠️ Gestion Administrative

### Bloquer un Compte

```java
@PostMapping("/admin/users/{userId}/block")
public ResponseEntity<?> blockUser(@PathVariable Integer userId) {
    User user = userService.findById(userId);
    user.setStatus(AccountStatus.BLOCKED);
    userService.updateUser(user);
    
    return ResponseEntity.ok(new ResponseMessage("User blocked successfully"));
}
```

### Réactiver un Compte

```java
@PostMapping("/admin/users/{userId}/activate")
public ResponseEntity<?> activateUser(@PathVariable Integer userId) {
    User user = userService.findById(userId);
    user.setStatus(AccountStatus.ACTIVE);
    userService.updateUser(user);
    
    return ResponseEntity.ok(new ResponseMessage("User activated successfully"));
}
```

### Désactiver son Propre Compte

```java
@PostMapping("/profile/deactivate")
public ResponseEntity<?> deactivateAccount(Principal principal) {
    User user = userService.findByEmail(principal.getName());
    user.setStatus(AccountStatus.INACTIVE);
    userService.updateUser(user);
    
    return ResponseEntity.ok(new ResponseMessage("Account deactivated successfully"));
}
```

---

## 🗄️ Migration SQL

**Fichier:** `V1__Add_Account_Status.sql`

```sql
-- Ajout de la colonne status
ALTER TABLE user 
ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION';

-- Mettre tous les comptes existants à ACTIVE
UPDATE user 
SET status = 'ACTIVE';

-- Index pour performances
CREATE INDEX idx_user_status ON user(status);
```

**Exécution:**
```bash
# Si vous utilisez Flyway
.\mvnw.cmd flyway:migrate

# Ou manuellement dans MySQL
mysql -u root -p sendbyop < src/main/resources/db/migration/V1__Add_Account_Status.sql
```

---

## 📊 Tableau de Comparaison

### Avant vs Après

| Aspect | Avant | Après |
|--------|-------|-------|
| **Vérification compte** | `Customer.emailVerified` | `User.status` |
| **Vérification profil** | `Customer.emailVerified` | `Customer.emailVerified` |
| **Blocage compte** | ❌ Impossible | ✅ `User.status = BLOCKED` |
| **Désactivation** | ❌ Impossible | ✅ `User.status = INACTIVE` |
| **Login non vérifié** | ✅ Autorisé | ❌ Refusé + Email renvoyé |
| **Gestion admin** | ❌ Limitée | ✅ Complète |

---

## 🎯 Cas d'Usage

### Scénario 1: Nouvel Utilisateur

1. S'inscrit → `User.status = PENDING_VERIFICATION`
2. Tente de se connecter → ❌ Refusé + Email renvoyé
3. Clique sur le lien → `User.status = ACTIVE`
4. Se connecte → ✅ Autorisé

### Scénario 2: Utilisateur Frauduleux

1. Admin détecte une fraude
2. Admin bloque le compte → `User.status = BLOCKED`
3. Utilisateur tente de se connecter → ❌ Refusé avec message
4. Utilisateur contacte le support

### Scénario 3: Utilisateur Inactif

1. Utilisateur désactive son compte → `User.status = INACTIVE`
2. Tente de se connecter plus tard → ❌ Refusé
3. Demande réactivation → Admin ou self-service
4. Compte réactivé → `User.status = ACTIVE`

---

## ✅ Checklist de Déploiement

- [ ] Ajouter la colonne `status` à la table `user`
- [ ] Mettre tous les comptes existants à `ACTIVE`
- [ ] Déployer le nouveau code
- [ ] Tester l'inscription (vérifier statut `PENDING_VERIFICATION`)
- [ ] Tester le login avant vérification (doit être refusé)
- [ ] Tester la vérification d'email (statut → `ACTIVE`)
- [ ] Tester le login après vérification (doit être autorisé)
- [ ] Tester le blocage/déblocage d'un compte (admin)
- [ ] Mettre à jour la documentation API

---

## 📚 Ressources

- **Guide inscription:** `TRANSACTIONAL_SIGNUP_GUIDE.md`
- **Configuration sécurité:** `SecurityConfig.java`
- **Enum statuts:** `AccountStatus.java`
- **Service inscription:** `UserRegistrationService.java`

---

**Date de création:** 21 octobre 2025  
**Fichiers modifiés:**
- `User.java` (ajout champ `status`)
- `AccountStatus.java` (nouveau enum)
- `UserRegistrationService.java` (gestion statuts)
- `AuthServiceImpl.java` (vérification au login)
- `V1__Add_Account_Status.sql` (migration BD)

**Type de modification:** Architecture de gestion des statuts de compte
