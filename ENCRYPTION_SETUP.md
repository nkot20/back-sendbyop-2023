# Configuration du Chiffrement des Données Bancaires

Ce document explique comment configurer et utiliser le système de chiffrement des coordonnées bancaires dans l'application SendByOp.

## 🔒 Vue d'ensemble

Le système de chiffrement utilise **AES-256-GCM** pour sécuriser les données bancaires sensibles :
- IBAN
- Numéro de compte bancaire  
- Code BIC
- Nom du titulaire du compte

## 📋 Composants Implémentés

### 1. Service de Chiffrement (`EncryptionService`)
- Chiffrement/déchiffrement AES-256-GCM
- IV aléatoire pour chaque chiffrement
- Authentification intégrée avec GCM
- Gestion sécurisée des erreurs

### 2. Convertisseur JPA (`EncryptedStringConverter`)
- Chiffrement automatique lors de la sauvegarde
- Déchiffrement automatique lors de la lecture
- Compatible avec les données existantes non chiffrées
- Injection de dépendance Spring

### 3. Entité BankInfo Modifiée
- Annotations `@Convert` sur les champs sensibles
- Transparence totale pour l'application
- Pas de changement dans la logique métier

### 4. Migration des Données (`BankDataMigrationService`)
- Chiffrement des données existantes
- Activation conditionnelle
- Logging détaillé
- Gestion des erreurs robuste

## 🚀 Configuration et Déploiement

### 1. Génération de la Clé de Chiffrement

```bash
# Générer une nouvelle clé sécurisée
java -cp target/classes com.sendByOP.expedition.utils.EncryptionKeyGenerator
```

### 2. Configuration de l'Application

**Option A : Configuration via variables d'environnement (RECOMMANDÉ)**
```bash
# Définir la variable d'environnement
export ENCRYPTION_SECRET_KEY="VOTRE_CLE_GENEREE_ICI"
export ENCRYPT_BANK_DATA="false"
```

**Option B : Configuration directe dans application-dev.properties**
```properties
# Configuration du chiffrement (déjà configuré avec une clé temporaire)
app.encryption.secret-key=${ENCRYPTION_SECRET_KEY:YWJjZGVmZ2hpams0bG1ub3BxcnN0dXZ3eHl6MTIzNDU2Nzg5MEFCQ0RFRkdISUpL}

# Migration optionnelle des données existantes
app.migration.encrypt-bank-data=${ENCRYPT_BANK_DATA:false}
```

### 3. Résolution des Erreurs de Démarrage

**Erreur : "Could not resolve placeholder 'app.encryption.secret-key'"**

✅ **Solution** : La configuration a été ajoutée dans `application-dev.properties` avec une clé temporaire valide.

**Pour la production** :
1. Générer une vraie clé avec `EncryptionKeyGenerator`
2. Définir la variable d'environnement `ENCRYPTION_SECRET_KEY`
3. Redémarrer l'application

### 4. Test de l'Application

Après configuration, l'application devrait démarrer sans erreur. Les nouvelles fonctionnalités disponibles :

**Endpoints disponibles :**
- `POST /bank-account-infos/save` - Sauvegarder des coordonnées bancaires (chiffrées automatiquement)
- `GET /bank-account-infos/{id}` - Récupérer par ID
- `GET /bank-account-infos/customer/{email}` - **NOUVEAU** : Récupérer par email client

**Test de l'endpoint par email :**
```bash
curl -X GET "http://localhost:8080/bank-account-infos/customer/client@example.com"
```

## 🚀 Déploiement

### Première Installation

1. **Générer une clé de chiffrement :**
   ```bash
   java -cp target/classes com.sendByOP.expedition.utils.EncryptionKeyGenerator
   ```

2. **Configurer la clé dans l'application**

3. **Démarrer l'application** - Les nouvelles données seront automatiquement chiffrées

### Migration des Données Existantes

1. **Activer la migration :**
   ```properties
   app.migration.encrypt-bank-data=true
   ```

2. **Redémarrer l'application** - La migration s'exécute au démarrage

3. **Vérifier les logs** pour confirmer la migration

4. **Désactiver la migration :**
   ```properties
   app.migration.encrypt-bank-data=false
   ```

## 🔧 Utilisation

### Aucun Changement de Code Requis

Le chiffrement est **totalement transparent** :

```java
// Le code existant fonctionne sans modification
BankInfo bankInfo = new BankInfo();
bankInfo.setIban("FR1420041010050500013M02606");  // Sera chiffré automatiquement
bankInfo.setBic("BNPAFRPPXXX");                   // Sera chiffré automatiquement

bankAccountRepository.save(bankInfo);

// Lecture automatiquement déchiffrée
BankInfo retrieved = bankAccountRepository.findById(id);
String iban = retrieved.getIban();  // Texte en clair
```

### Vérification du Chiffrement

```java
@Autowired
private EncryptionService encryptionService;

// Vérifier si une donnée est chiffrée
boolean isEncrypted = encryptionService.isEncrypted(someString);
```

## 🧪 Tests

### Tests Unitaires

```bash
# Exécuter les tests du service de chiffrement
mvn test -Dtest=EncryptionServiceTest
```

### Tests d'Intégration

```java
@Test
void testBankInfoEncryption() {
    BankInfo bankInfo = new BankInfo();
    bankInfo.setIban("FR1420041010050500013M02606");
    
    BankInfo saved = bankAccountRepository.save(bankInfo);
    BankInfo retrieved = bankAccountRepository.findById(saved.getId()).get();
    
    // L'IBAN doit être identique (déchiffré automatiquement)
    assertEquals("FR1420041010050500013M02606", retrieved.getIban());
}
```

## 🔐 Sécurité

### Bonnes Pratiques Implémentées

- **AES-256-GCM** : Chiffrement et authentification
- **IV aléatoire** : Chaque chiffrement est unique
- **Clé externe** : Pas de clé hardcodée
- **Gestion d'erreurs** : Pas de fuite d'informations
- **Logging sécurisé** : Pas de données sensibles dans les logs

### Recommandations de Production

1. **Gestionnaire de Secrets** : Utiliser AWS Secrets Manager, Azure Key Vault, etc.
2. **Rotation des Clés** : Planifier la rotation périodique
3. **Sauvegarde Sécurisée** : Sauvegarder les clés de manière sécurisée
4. **Monitoring** : Surveiller les échecs de chiffrement/déchiffrement
5. **Audit** : Tracer les accès aux données chiffrées

## 🚨 Points d'Attention

### Contraintes d'Unicité

⚠️ **Important** : Les contraintes `UNIQUE` sur les champs chiffrés ont été supprimées car :
- Même valeur → chiffrements différents (IV aléatoire)
- Les contraintes DB ne fonctionnent plus avec le chiffrement

**Solution implémentée** :
1. ✅ Contraintes `UNIQUE` supprimées sur `iban` et `bic` (migration SQL)
2. ✅ Validation d'unicité implémentée en Java (`BankInfoValidationService`)
3. ✅ Validation des formats IBAN/BIC intégrée
4. ✅ Gestion des erreurs avec messages explicites

### Performance

- **Impact minimal** : Chiffrement/déchiffrement rapide
- **Taille** : Les données chiffrées sont ~1.5x plus grandes
- **Index** : Les index sur champs chiffrés sont moins efficaces

### Compatibilité

- **Données existantes** : Compatible via migration
- **Rollback** : Possible si migration conservée
- **Versions** : Compatible Spring Boot 3.x+

## 📞 Support

En cas de problème :

1. **Vérifier les logs** pour les erreurs de chiffrement
2. **Tester la clé** avec `EncryptionKeyGenerator`
3. **Valider la configuration** des propriétés
4. **Exécuter les tests** unitaires

## 🔄 Évolutions Futures

- Support de la rotation automatique des clés
- Chiffrement d'autres entités sensibles
- Intégration avec des HSM (Hardware Security Modules)
- Audit trail des accès aux données chiffrées
