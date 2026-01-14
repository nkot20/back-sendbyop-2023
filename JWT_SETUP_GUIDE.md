# 🔐 Guide de Configuration JWT - SendByOp

## 📋 Vue d'ensemble

Ce guide explique comment générer et configurer un JWT secret sécurisé pour l'authentification dans SendByOp.

## 🎯 Pourquoi un JWT Secret Fort ?

Un JWT (JSON Web Token) secret est utilisé pour :
- ✅ Signer les tokens d'authentification
- ✅ Vérifier l'intégrité des tokens
- ✅ Empêcher la falsification des tokens

**⚠️ IMPORTANT** : Un secret faible peut être cracké, compromettant la sécurité de toute l'application !

## 🔑 Générer un JWT Secret Sécurisé

### Option 1 : Ligne de Commande (Linux/Mac)

```bash
# Générer une clé de 512 bits (64 octets) en Base64
openssl rand -base64 64 | tr -d '\n'
```

**Exemple de sortie :**
```
7K9mP2nQ5rT8wY1aB4cD6eF9gH2jK5mN8pQ1rS4tU7vW0xY3zA6bC9dE2fG5hJ8kL1mN4oP7qR0sT3uV6wX9yZ2aB5cD8eF1gH4jK7mN0pQ3rS6tU9vW2xY5zA8bC1dE4fG7hJ0kL3mN6oP9qR2sT5uV8wX1yZ4aB7cD0eF3gH6jK9mN2pQ5rS8tU1vW4xY7zA0bC3dE6fG9hJ2kL5mN8oP1qR4sT7uV0wX3yZ6aB9cD2eF5gH8jK1mN4oP7qR0sT3uV6wX9yZ
```

### Option 2 : PowerShell (Windows)

```powershell
# Générer une clé aléatoire de 64 octets
$bytes = New-Object byte[] 64
[Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

### Option 3 : Node.js

```javascript
// Installer crypto (déjà inclus dans Node.js)
const crypto = require('crypto');
console.log(crypto.randomBytes(64).toString('base64'));
```

### Option 4 : Python

```python
import secrets
import base64
print(base64.b64encode(secrets.token_bytes(64)).decode('utf-8'))
```

### Option 5 : Outil en Ligne (⚠️ Moins Sécurisé)

**Pour le développement uniquement** :
- https://generate-secret.vercel.app/64
- https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx

**⚠️ ATTENTION** : Ne jamais utiliser de secrets générés en ligne pour la production !

## 📁 Configuration du Fichier .env

### Étape 1 : Créer le fichier .env

```bash
# Copier le fichier exemple
cp .env.example .env
```

### Étape 2 : Éditer le fichier .env

Ouvrez `.env` et remplacez les valeurs :

```properties
# JWT CONFIGURATION
JWT_SECRET=VOTRE_SECRET_GENERE_ICI
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

**Exemple complet :**
```properties
JWT_SECRET=7K9mP2nQ5rT8wY1aB4cD6eF9gH2jK5mN8pQ1rS4tU7vW0xY3zA6bC9dE2fG5hJ8kL1mN4oP7qR0sT3uV6wX9yZ2aB5cD8eF1gH4jK7mN0pQ3rS6tU9vW2xY5zA8bC1dE4fG7hJ0kL3mN6oP9qR2sT5uV8wX1yZ4aB7cD0eF3gH6jK9mN2pQ5rS8tU1vW4xY7zA0bC3dE6fG9hJ2kL5mN8oP1qR4sT7uV0wX3yZ6aB9cD2eF5gH8jK1mN4oP7qR0sT3uV6wX9yZ
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

### Étape 3 : Vérifier le .gitignore

Assurez-vous que `.env` est dans `.gitignore` :

```gitignore
# Environment variables
.env
.env.local
.env.*.local
```

## ⏱️ Configuration des Durées de Validité

### JWT_EXPIRATION (Token d'Accès)

Durée de validité du token JWT principal.

| Durée | Millisecondes | Recommandation |
|-------|---------------|----------------|
| 15 minutes | 900000 | ✅ Très sécurisé (recommandé pour production) |
| 1 heure | 3600000 | ✅ Sécurisé |
| 24 heures | 86400000 | ⚠️ Acceptable pour développement |
| 7 jours | 604800000 | ❌ Trop long (non recommandé) |

**Recommandation** : 15-60 minutes pour la production

### JWT_REFRESH_EXPIRATION (Token de Rafraîchissement)

Durée de validité du refresh token (pour obtenir un nouveau token d'accès).

| Durée | Millisecondes | Recommandation |
|-------|---------------|----------------|
| 7 jours | 604800000 | ✅ Recommandé |
| 30 jours | 2592000000 | ⚠️ Acceptable |
| 90 jours | 7776000000 | ❌ Trop long |

**Recommandation** : 7-30 jours

### Exemple de Configuration Sécurisée

```properties
# Production : Tokens courts pour plus de sécurité
JWT_EXPIRATION=900000          # 15 minutes
JWT_REFRESH_EXPIRATION=604800000  # 7 jours

# Développement : Tokens plus longs pour faciliter le dev
JWT_EXPIRATION=86400000        # 24 heures
JWT_REFRESH_EXPIRATION=2592000000  # 30 jours
```

## 🔐 Bonnes Pratiques de Sécurité

### 1. Secrets Différents par Environnement

```properties
# Développement
JWT_SECRET=dev_secret_here...

# Staging
JWT_SECRET=staging_secret_here...

# Production
JWT_SECRET=prod_secret_here...
```

**⚠️ Ne JAMAIS utiliser le même secret pour dev et prod !**

### 2. Rotation des Secrets

Changez régulièrement vos secrets JWT :
- ✅ Tous les 3-6 mois en production
- ✅ Immédiatement en cas de suspicion de compromission
- ✅ Après le départ d'un développeur ayant eu accès

### 3. Longueur Minimale

| Algorithme | Longueur Minimale | Recommandé |
|------------|-------------------|------------|
| HS256 | 256 bits (32 octets) | 512 bits (64 octets) |
| HS384 | 384 bits (48 octets) | 512 bits (64 octets) |
| HS512 | 512 bits (64 octets) | 512 bits (64 octets) |

**SendByOp utilise HS512** → Utilisez au minimum 64 octets (512 bits)

### 4. Stockage Sécurisé

**✅ Bonnes pratiques :**
- Variables d'environnement
- Gestionnaire de secrets (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
- Fichiers .env (non commités)

**❌ Mauvaises pratiques :**
- Hardcodé dans le code
- Commité dans Git
- Stocké en clair dans la base de données

### 5. Validation du Secret

Vérifiez que votre secret est assez fort :

```java
// Minimum 64 caractères pour HS512
if (jwtSecret.length() < 64) {
    throw new IllegalArgumentException("JWT secret trop court ! Minimum 64 caractères.");
}
```

## 🧪 Tester la Configuration

### Test 1 : Vérifier les Variables d'Environnement

```bash
# Linux/Mac
echo $JWT_SECRET

# Windows PowerShell
echo $env:JWT_SECRET
```

### Test 2 : Tester l'Authentification

```bash
# 1. S'inscrire
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "Test123!",
    "phoneNumber": "+33612345678",
    "country": "France",
    "address": "123 Test St"
  }'

# 2. Se connecter
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'

# Vous devriez recevoir un token JWT
```

### Test 3 : Vérifier le Token

Copiez le token reçu et décodez-le sur https://jwt.io pour vérifier :
- ✅ L'algorithme est HS512
- ✅ Les claims sont corrects (email, roles, exp)
- ✅ La signature est valide

## 🚨 Que Faire en Cas de Compromission ?

Si vous pensez que votre JWT secret a été compromis :

### 1. Générer un Nouveau Secret

```bash
openssl rand -base64 64 | tr -d '\n'
```

### 2. Mettre à Jour le .env

```properties
JWT_SECRET=NOUVEAU_SECRET_ICI
```

### 3. Redémarrer l'Application

```bash
# Arrêter l'application
# Redémarrer avec le nouveau secret
mvn spring-boot:run
```

### 4. Invalider Tous les Tokens Existants

Tous les tokens signés avec l'ancien secret seront automatiquement invalides.

### 5. Notifier les Utilisateurs

Envoyez un email demandant aux utilisateurs de se reconnecter.

## 📊 Monitoring et Logs

### Logs à Surveiller

```java
// Tentatives d'authentification échouées
log.warn("Invalid JWT token: {}", e.getMessage());

// Tokens expirés
log.info("JWT token expired for user: {}", username);

// Signatures invalides
log.error("JWT signature validation failed");
```

### Métriques à Tracker

- Nombre de tokens générés par jour
- Taux d'échec de validation
- Durée moyenne de session
- Nombre de refresh token utilisés

## 🔧 Dépannage

### Problème : "JWT secret not configured"

**Solution :**
```bash
# Vérifier que JWT_SECRET est défini
echo $JWT_SECRET

# Si vide, définir la variable
export JWT_SECRET="votre_secret_ici"
```

### Problème : "Invalid JWT signature"

**Causes possibles :**
1. Secret JWT incorrect
2. Token modifié
3. Secret changé après génération du token

**Solution :** Vérifier que le secret dans `.env` correspond à celui utilisé pour générer le token.

### Problème : "JWT expired"

**Solution :**
1. Augmenter `JWT_EXPIRATION` (développement)
2. Implémenter le refresh token (production)
3. Demander à l'utilisateur de se reconnecter

## 📚 Ressources

- [JWT.io](https://jwt.io) - Décodeur de tokens
- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [RFC 7519 - JWT Specification](https://tools.ietf.org/html/rfc7519)

---

**Dernière mise à jour** : 2024
**Version** : 1.0
**Auteur** : Équipe SendByOp
