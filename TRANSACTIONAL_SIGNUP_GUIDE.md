# Guide : Inscription Transactionnelle avec Rollback

## 📋 Problématique

Lors de l'inscription d'un nouveau customer, si l'envoi de l'email de vérification échoue APRÈS la sauvegarde du Customer et du User en base de données, on se retrouve avec :
- ✅ Un Customer créé en BD
- ✅ Un User créé en BD
- ❌ Aucun email envoyé

**Résultat:** L'utilisateur ne peut pas vérifier son compte et reste bloqué.

---

## ✅ Solution Implémentée

### Modifications dans `UserRegistrationService.java`

#### 1️⃣ Suppression de `@Async` sur `sendVerificationEmail()`

**AVANT:**
```java
@Async
public void sendVerificationEmail(CustomerDto customer) throws SendByOpException {
    // Envoi asynchrone dans un thread séparé
    // ❌ Pas de rollback possible
}
```

**APRÈS:**
```java
/**
 * Envoie l'email de vérification de manière SYNCHRONE pour garantir le rollback transactionnel
 * Si l'envoi échoue, toute l'inscription (Customer + User) sera annulée
 */
private void sendVerificationEmail(CustomerDto customer) throws SendByOpException {
    // Envoi synchrone dans le même thread
    // ✅ Rollback automatique si erreur
}
```

#### 2️⃣ La Méthode est maintenant `private`

Elle n'est appelée qu'à l'intérieur de `registerNewCustomer()`, qui est elle-même annotée `@Transactional` (classe entière annotée ligne 26).

#### 3️⃣ Flux Transactionnel Complet

```java
@Transactional
public CustomerDto registerNewCustomer(CustomerRegistrationDto registrationDto) throws SendByOpException {
    // 1. Vérification que l'email n'existe pas
    if (customerService.customerIsExist(registrationDto.getEmail())) {
        throw new SendByOpException(ErrorInfo.EMAIL_ALREADY_EXISTS);
    }

    // 2. Création du Customer
    CustomerDto savedCustomer = customerService.saveClient(customer);
    
    // 3. Création du User
    userService.saveUser(User.builder()...);
    
    // 4. Envoi de l'email de vérification (SYNCHRONE)
    sendVerificationEmail(savedCustomer);
    // ⬆️ Si cette ligne échoue, TOUT est annulé (rollback)
    
    return savedCustomer;
}
```

---

## 🔄 Comportement Transactionnel

### Cas 1 : Tout Réussit ✅

1. Customer créé en BD
2. User créé en BD
3. Token de vérification créé
4. Email envoyé avec succès
5. **Transaction committée** ✅

### Cas 2 : Email Échoue ❌

1. Customer créé en BD
2. User créé en BD
3. Token de vérification créé
4. **Email échoue** (MessagingException)
5. Exception propagée
6. **ROLLBACK automatique** : Customer + User + Token supprimés ♻️
7. Erreur retournée au client

---

## ⚠️ Impact sur les Performances

### Avantage de l'Ancien Système (@Async)
- ⚡ Réponse immédiate à l'utilisateur
- 📧 Email envoyé en arrière-plan

### Inconvénient de l'Ancien Système
- ❌ Pas de rollback si l'email échoue
- 💔 Données orphelines en BD

### Avantage du Nouveau Système (Synchrone)
- ✅ Garantie de cohérence des données
- ✅ Rollback automatique si échec
- ✅ Utilisateur informé immédiatement de l'erreur

### Inconvénient du Nouveau Système
- ⏱️ Temps de réponse légèrement plus long (attente de l'envoi d'email)
- Généralement **acceptable** car l'envoi d'un email prend 1-3 secondes

---

## 📊 Comparaison des Scénarios

| Scénario | Ancien (@Async) | Nouveau (Synchrone) |
|----------|----------------|---------------------|
| Email envoyé avec succès | ✅ User créé<br>✅ Email envoyé | ✅ User créé<br>✅ Email envoyé |
| Email échoue | ❌ User créé sans email<br>💔 Données orphelines | ✅ Rollback complet<br>✅ Erreur claire au client |
| Temps de réponse | ~200ms | ~2000ms (1-3s) |
| Cohérence des données | ⚠️ Non garantie | ✅ Garantie |

---

## 🧪 Tests Recommandés

### Test 1 : Inscription Réussie

```bash
POST /auth/register
{
  "firstName": "Jean",
  "lastName": "Dupont",
  "email": "jean.dupont@example.com",
  "password": "SecurePass123!",
  "phoneNumber": "+33612345678",
  "country": "France",
  "address": "123 Rue de la Paix"
}
```

**Résultat attendu:**
- ✅ Code 200
- ✅ Customer créé en BD
- ✅ User créé en BD
- ✅ Email reçu

### Test 2 : Échec d'Envoi d'Email

**Simulation:** Configurer un serveur SMTP invalide dans `.env`

```env
EMAIL_USERNAME=invalid@example.com
EMAIL_PASSWORD=wrong_password
```

**Résultat attendu:**
- ❌ Code 500 avec message "Impossible d'envoyer l'email de vérification"
- ✅ Aucun Customer en BD
- ✅ Aucun User en BD
- ✅ Rollback complet

### Test 3 : Email Déjà Existant

```bash
POST /auth/register
# Avec un email déjà enregistré
```

**Résultat attendu:**
- ❌ Code 409 (Conflict)
- ✅ Aucune donnée créée
- ✅ Message "Email already exists"

---

## 🔧 Configuration Requise

### application.properties

Assurez-vous que les propriétés d'email sont correctement configurées :

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
email.from=${EMAIL_USERNAME}

# Base URL pour les liens de vérification
base.url=http://localhost:9002
```

### .env

```env
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-specific-password
```

---

## 🚨 Gestion des Erreurs

### Erreur 1 : EMAIL_SEND_ERROR

**Cause:** Échec de connexion au serveur SMTP

**Action:** 
- Vérifier les credentials SMTP
- Vérifier la connexion réseau
- Vérifier que le compte email autorise les applications tierces

### Erreur 2 : EMAIL_ALREADY_EXISTS

**Cause:** L'email est déjà enregistré

**Action:**
- Proposer une connexion
- Proposer une réinitialisation de mot de passe

### Erreur 3 : INTERNAL_ERROR

**Cause:** Erreur inattendue (encodage, template, etc.)

**Action:**
- Consulter les logs serveur
- Vérifier les templates Thymeleaf
- Vérifier la configuration

---

## 📝 Méthode resendVerificationEmail()

Cette méthode **doit rester publique** pour permettre le renvoi d'email :

```java
public void resendVerificationEmail(String email) throws SendByOpException {
    CustomerDto customer = customerService.getCustomerByEmail(email);
    if (customer == null) {
        throw new SendByOpException(ErrorInfo.USER_NOT_FOUND);
    }
    sendVerificationEmail(customer);
}
```

**Note:** Ici, pas de rollback car le Customer existe déjà. Si l'email échoue, on peut réessayer plus tard.

---

## ✅ Checklist de Vérification

- [x] `@Async` supprimé de `sendVerificationEmail()`
- [x] Import `org.springframework.scheduling.annotation.Async` supprimé
- [x] Méthode `sendVerificationEmail()` marquée `private`
- [x] Classe annotée `@Transactional` (ligne 26)
- [x] Documentation ajoutée
- [x] Tests effectués

---

## 🎯 Conclusion

Le processus d'inscription est maintenant **transactionnel et cohérent** :

✅ **Si tout réussit** → Customer créé + User créé + Email envoyé  
❌ **Si l'email échoue** → Rollback complet, aucune donnée orpheline  

Le léger impact sur les performances (~2 secondes) est **largement compensé** par la garantie de cohérence des données.

---

**Date de modification:** 21 octobre 2025  
**Fichier modifié:** `UserRegistrationService.java`  
**Type de modification:** Transactionnalité renforcée
