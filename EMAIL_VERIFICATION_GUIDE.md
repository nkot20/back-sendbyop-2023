# 📧 Guide de Vérification d'Email - SendByOp

## 📋 Vue d'ensemble

Ce document décrit le système complet de vérification d'email pour l'inscription des clients sur la plateforme SendByOp.

## 🔄 Flux de Vérification

### 1. Inscription du Client
```
POST /api/v1/auth/register
```

**Requête :**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+33612345678",
  "password": "SecurePassword123!",
  "country": "France",
  "address": "123 Rue de Paris"
}
```

**Réponse :**
```json
{
  "message": "Registration successful. Please check your email for verification."
}
```

**Processus :**
1. Validation des données d'inscription
2. Vérification que l'email n'existe pas déjà
3. Création du compte client (statut: non vérifié)
4. Création du compte utilisateur avec mot de passe chiffré
5. Génération d'un token de vérification (64 caractères, valide 24h)
6. Envoi automatique de l'email de vérification

### 2. Réception de l'Email

Le client reçoit un email HTML stylisé contenant :
- Message de bienvenue personnalisé
- Bouton "Vérifier mon email"
- Lien de vérification (en cas de problème avec le bouton)
- Avertissement d'expiration (24 heures)
- Instructions de contact en cas de problème

**Format du lien :**
```
{baseUrl}/verification/verify?code={token}
```

### 3. Vérification de l'Email

```
POST /customer/verify/email/{token}
```

**Réponse succès :**
```json
{
  "message": "Email verified successfully"
}
```

**Réponse erreur - Token expiré :**
```json
{
  "error": "TOKEN_EXPIRED",
  "message": "Verification token has expired"
}
```

**Réponse erreur - Token invalide :**
```json
{
  "error": "TOKEN_INVALID",
  "message": "Invalid verification token"
}
```

### 4. Renvoi de l'Email de Vérification

Si le client n'a pas reçu l'email ou si le token a expiré :

```
POST /customer/resend/email/{email}
```

**Exemple :**
```
POST /customer/resend/email/john.doe@example.com
```

**Réponse :**
```json
{
  "message": "Verification email sent successfully"
}
```

## 🏗️ Architecture Technique

### Composants Principaux

#### 1. **UserRegistrationService**
- `registerNewCustomer()` : Inscription et envoi d'email
- `sendVerificationEmail()` : Envoi asynchrone de l'email
- `verifyCustomerEmail()` : Validation du token
- `resendVerificationEmail()` : Renvoi de l'email

#### 2. **VerifyTokenService**
- `save()` : Génération et sauvegarde du token
- `verifyToken()` : Validation du token (valide/expiré/invalide)
- `cleanupExpiredTokens()` : Nettoyage automatique quotidien

#### 3. **SendMailService**
- `sendVerificationEmail()` : Envoi d'email HTML avec gestion d'erreurs

#### 4. **VerifyToken (Entité)**
```java
- tokenid: Integer (PK)
- email: String
- token: String (64 caractères)
- expiratedToken: Date (TIMESTAMP)
```

### Base de Données

**Table : verify_token**
```sql
CREATE TABLE verify_token (
    tokenid INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    expirated_token TIMESTAMP NOT NULL,
    INDEX idx_email (email),
    INDEX idx_token (token)
);
```

## 🔧 Configuration

### Variables d'Environnement

```properties
# Configuration email (application-dev.properties)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
email.from=${EMAIL_USERNAME}

# URL de base pour les liens de vérification
base.url=http://localhost:8080
```

### Activation du Scheduling

Pour activer le nettoyage automatique des tokens expirés, assurez-vous que `@EnableScheduling` est présent dans la classe principale :

```java
@SpringBootApplication
@EnableScheduling
public class ExpeditionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpeditionApplication.class, args);
    }
}
```

## ✨ Améliorations Implémentées

### 1. **Template d'Email Professionnel**
- Design HTML responsive
- Couleurs de marque SendByOp
- Bouton CTA visible
- Lien de secours
- Avertissements clairs

### 2. **Gestion Robuste des Erreurs**
- Logging détaillé à chaque étape
- Re-lancement des exceptions pour traçabilité
- Messages d'erreur explicites

### 3. **Prévention des Doublons**
- Suppression automatique de l'ancien token lors d'un renvoi
- Un seul token valide par email à la fois

### 4. **Nettoyage Automatique**
- Tâche planifiée quotidienne (2h du matin)
- Suppression des tokens expirés
- Logging du nombre de tokens supprimés

### 5. **Type de Date Corrigé**
- Utilisation de `TIMESTAMP` au lieu de `DATE`
- Précision à la seconde pour l'expiration

## 🧪 Tests

### Test Manuel

**1. Inscription**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "phoneNumber": "+33612345678",
    "password": "Test123!",
    "country": "France",
    "address": "123 Test Street"
  }'
```

**2. Vérification**
```bash
curl -X POST http://localhost:8080/customer/verify/email/{TOKEN}
```

**3. Renvoi d'email**
```bash
curl -X POST http://localhost:8080/customer/resend/email/test@example.com
```

### Vérification des Logs

```bash
# Logs d'inscription
grep "user registration" logs/application.log

# Logs d'envoi d'email
grep "Email de vérification envoyé" logs/application.log

# Logs de vérification
grep "Token valide vérifié" logs/application.log

# Logs de nettoyage
grep "Nettoyage terminé" logs/application.log
```

## 🔍 Débogage

### Problèmes Courants

**1. Email non reçu**
- Vérifier la configuration SMTP
- Vérifier les logs pour les erreurs d'envoi
- Vérifier le dossier spam
- Tester avec un autre fournisseur d'email

**2. Token expiré**
- Utiliser l'endpoint de renvoi
- Vérifier que l'expiration est bien à 24h

**3. Token invalide**
- Vérifier que le token n'a pas été modifié
- Vérifier qu'il n'a pas été supprimé de la base

### Commandes SQL Utiles

```sql
-- Voir tous les tokens actifs
SELECT * FROM verify_token WHERE expirated_token > NOW();

-- Voir les tokens expirés
SELECT * FROM verify_token WHERE expirated_token < NOW();

-- Compter les tokens par statut
SELECT 
    CASE 
        WHEN expirated_token > NOW() THEN 'Actif'
        ELSE 'Expiré'
    END as statut,
    COUNT(*) as nombre
FROM verify_token
GROUP BY statut;

-- Supprimer manuellement les tokens expirés
DELETE FROM verify_token WHERE expirated_token < NOW();
```

## 📊 Monitoring

### Métriques à Surveiller

1. **Taux de vérification** : Nombre de vérifications / Nombre d'inscriptions
2. **Temps moyen de vérification** : Temps entre inscription et vérification
3. **Taux de renvoi** : Nombre de renvois / Nombre d'inscriptions
4. **Tokens expirés** : Nombre de tokens qui expirent sans être utilisés

### Logs Importants

```
INFO  - Token de vérification email généré pour {email}
INFO  - Email de vérification envoyé avec succès à {email}
INFO  - Token valide vérifié pour l'email: {email}
WARN  - Token expiré pour l'email: {email}
WARN  - Token invalide reçu: {token}
ERROR - Erreur d'envoi d'email pour {email}: {message}
```

## 🚀 Prochaines Améliorations Possibles

1. **Notification de succès** : Email de confirmation après vérification
2. **Limite de renvoi** : Limiter le nombre de renvois par heure
3. **Analytics** : Tracker les taux de conversion
4. **Multi-langue** : Templates d'email en plusieurs langues
5. **SMS de secours** : Option de vérification par SMS
6. **Expiration configurable** : Permettre de configurer la durée de validité

## 📞 Support

En cas de problème avec le système de vérification d'email :
1. Vérifier les logs de l'application
2. Vérifier la configuration SMTP
3. Tester l'envoi d'email manuellement
4. Contacter l'équipe technique SendByOp

---

**Dernière mise à jour** : 2024
**Version** : 1.0
**Auteur** : Équipe SendByOp
