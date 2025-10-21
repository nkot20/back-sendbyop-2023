# Correction des Erreurs de Chiffrement et Clé Étrangère

## 📋 Résumé des Problèmes

### 1. ❌ Clé AES Invalide (48 bytes)
**Erreur:** `Invalid AES key length: 48 bytes`
**Cause:** La clé dans `.env.example` fait 48 bytes après décodage Base64, mais AES-256 requiert exactement 32 bytes.

### 2. ❌ Contrainte de Clé Étrangère
**Erreur:** `Cannot add or update a child row: a foreign key constraint fails`
**Cause:** Le mapper `BankInfoMapper` ne créait pas d'objet `Customer` complet, juste un ID.

---

## ✅ Solutions Appliquées

### Solution 1: Nouvelle Clé de Chiffrement AES-256

Une nouvelle clé de 32 bytes (256 bits) a été générée:
```
/EWAinroNLb04MYyxS8zxV+RvGM9m9HoaxeUOnPiwFk=
```

**Action Requise:** Mettre à jour votre fichier `.env`:

```env
# Dans votre fichier .env (ou créez-le depuis .env.example)
ENCRYPTION_SECRET_KEY=/EWAinroNLb04MYyxS8zxV+RvGM9m9HoaxeUOnPiwFk=
```

**⚠️ IMPORTANT:**
- **NE JAMAIS** commiter le fichier `.env` dans Git
- Utiliser une clé différente pour chaque environnement (dev/staging/prod)
- Sauvegarder cette clé de manière sécurisée

### Solution 2: Correction du Service BankAccountInfo

**Fichier:** `BankAccountInfoService.java`

**Modification:** La méthode `save()` récupère maintenant le `Customer` complet avant de sauvegarder:

```java
// Récupérer le Customer complet
CustomerDto customerDto = clientService.getClientById(bankInfo.getClientId());
if (customerDto == null) {
    throw new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
        "Customer not found with ID: " + bankInfo.getClientId());
}
Customer customer = customerMapper.toEntity(customerDto);

// Convert DTO to entity
BankInfo newBankInfo = bankInfoMapper.toEntity(bankInfo);
newBankInfo.setCustomer(customer); // Set the complete Customer object
```

---

## 🚀 Étapes de Déploiement

### 1. Mettre à Jour la Configuration

```bash
# 1. Ouvrir ou créer le fichier .env
cp .env.example .env  # Si .env n'existe pas

# 2. Éditer .env et mettre à jour:
ENCRYPTION_SECRET_KEY=/EWAinroNLb04MYyxS8zxV+RvGM9m9HoaxeUOnPiwFk=
```

### 2. Recompiler et Redémarrer l'Application

```powershell
# Option A: Avec le script automatisé
.\clean-and-rebuild.ps1

# Option B: Manuellement
.\mvnw.cmd clean package -DskipTests
# Puis redémarrer l'application Spring Boot
```

### 3. Tester l'Enregistrement des Coordonnées Bancaires

```bash
# Test avec curl (remplacer les valeurs)
curl -X POST http://localhost:9002/bank-account-infos/save \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "clientId": 1,
    "iban": "FR7630006000011234567890189",
    "bankAccount": "12345678901",
    "bankName": "BNP Paribas",
    "bic": "BNPAFRPP",
    "accountHolder": "Jean Dupont",
    "countryName": "France"
  }'
```

---

## 📝 Fichiers Modifiés

1. **`BankAccountInfoService.java`** ✅
   - Ajout de l'import `Customer`
   - Modification de la méthode `save()` pour récupérer le Customer complet

2. **`JwtResponse.java`** ✅
   - Ajout du champ `id` (Integer)
   - Mise à jour du constructeur

3. **`AuthServiceImpl.java`** ✅
   - Récupération de l'ID du customer lors du login
   - Ajout de l'ID dans la réponse JWT

---

## 🛠️ Scripts Utilitaires Créés

### 1. `generate-encryption-key.ps1`
Génère une clé AES-256 valide de 32 bytes.

**Usage:**
```powershell
.\generate-encryption-key.ps1
# La clé est générée et copiée dans le presse-papiers
```

### 2. `clean-and-rebuild.ps1`
Nettoie complètement le projet et recompile (résout les problèmes de cache IntelliJ).

**Usage:**
```powershell
.\clean-and-rebuild.ps1
# Suivre les instructions à l'écran
```

---

## 🔒 Sécurité

### Bonnes Pratiques

1. **Clés de Chiffrement:**
   - Utiliser une clé différente par environnement
   - Ne jamais commiter les clés dans Git
   - Rotation régulière des clés (tous les 6-12 mois)
   - Sauvegarder les clés dans un gestionnaire de secrets sécurisé

2. **Données Bancaires:**
   - Les données sont chiffrées en AES-256-GCM
   - Chaque chiffrement utilise un IV aléatoire
   - Le chiffrement est transparent (automatique via JPA)

3. **Tests:**
   - Vérifier que les données sont bien chiffrées en BD
   - Tester le déchiffrement lors de la lecture
   - Valider les contraintes d'unicité

---

## 🐛 Dépannage

### Erreur: "Invalid AES key length"
**Solution:** Vérifier que `ENCRYPTION_SECRET_KEY` dans `.env` fait exactement 44 caractères Base64 (32 bytes décodés).

### Erreur: "Customer not found"
**Solution:** Vérifier que le `clientId` envoyé existe bien dans la table `customer`.

### Erreur: "String cannot be resolved to a type"
**Solution:** Problème de cache IntelliJ. Exécuter:
```powershell
.\clean-and-rebuild.ps1
# Puis dans IntelliJ: File → Invalidate Caches / Restart
```

---

## ✅ Checklist de Vérification

- [ ] Fichier `.env` créé et configuré avec la nouvelle clé
- [ ] Application recompilée et redémarrée
- [ ] Test d'enregistrement de coordonnées bancaires réussi
- [ ] Données chiffrées vérifiées en base de données
- [ ] Lecture et déchiffrement fonctionnels
- [ ] Aucune erreur dans les logs

---

**Date de correction:** 21 octobre 2025
**Versions:**
- Spring Boot: 3.1.4
- Java: 17
- AES: 256-GCM
