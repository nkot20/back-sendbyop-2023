# Guide de Corrections CORS et Erreurs de Compilation

## Problèmes Résolus

### 1. Configuration CORS pour Production ✅

**Problème:** Le backend en production sur Render bloquait les requêtes du frontend en développement avec des erreurs CORS.

**Solution:** Configuration CORS dynamique basée sur des variables d'environnement.

#### Modifications apportées:

1. **SecurityConfig.java**
   - Ajout d'une propriété configurable `cors.allowed-origins`
   - Support de plusieurs origines (séparées par des virgules)
   - Ajout de logs pour debug des origines autorisées
   - Ajout de `PATCH` dans les méthodes autorisées
   - Ajout de `Authorization` dans les headers exposés
   - Configuration du cache des requêtes preflight (1 heure)

2. **application-dev.properties**
   ```properties
   cors.allowed-origins=http://localhost:4200,http://localhost:4201,http://127.0.0.1:4200
   ```

3. **application-prod.properties**
   ```properties
   cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200,https://sendbyop.com,https://www.sendbyop.com}
   ```

#### Configuration sur Render:

Ajoutez la variable d'environnement suivante sur Render :

```
CORS_ALLOWED_ORIGINS=http://localhost:4200,https://votre-frontend.com,https://www.votre-frontend.com
```

⚠️ **Important:** Remplacez les URLs de production par les vraies URLs de votre frontend déployé.

### 2. Erreurs de Compilation Date/LocalDateTime ✅

**Problème:** Incompatibilités de types entre `java.util.Date` (utilisé dans `BaseEntity`) et `java.time.LocalDateTime` (utilisé dans certains champs spécifiques et DTOs).

**Solution:** Création d'une classe utilitaire `DateTimeUtils` pour les conversions entre les deux types.

#### Fichiers créés/modifiés:

1. **DateTimeUtils.java** (nouveau)
   - `dateToLocalDateTime(Date date)`: Convertit Date → LocalDateTime
   - `localDateTimeToDate(LocalDateTime ldt)`: Convertit LocalDateTime → Date
   - Méthodes utilitaires `now()` et `nowAsDate()`

2. **PaymentResponse.java**
   - `createdAt` changé de `Date` à `LocalDateTime` pour cohérence

3. **PaymentService.java**
   - Import de `DateTimeUtils`
   - Conversion de `transaction.getCreatedAt()` avec `DateTimeUtils.dateToLocalDateTime()`
   - Ligne 499: Correction de l'expression ternaire mixant Date et LocalDateTime

4. **Providers de paiement** (OrangeMoneyProvider, MtnMobileMoneyProvider, CreditCardProvider, PayPalProvider)
   - Import de `DateTimeUtils`
   - Conversion de `transaction.getCreatedAt()` dans les réponses

5. **PlatformSettingsService.java**
   - Suppression des appels manuels à `setUpdatedAt(LocalDateTime.now())`
   - Utilisation de `@UpdateTimestamp` de BaseEntity (gestion automatique)

### 3. Correction URL Frontend ✅

**Problème:** L'URL dans `environment.ts` était incorrecte (manquait `/api/v1`).

**Solution:** Mise à jour de l'URL pour inclure le context-path.

#### Modification:

**environment.ts** (développement)
```typescript
// Avant
apiUrl: 'https://back-sendbyop-2023.onrender.com'

// Après
apiUrl: 'https://back-sendbyop-2023.onrender.com/api/v1'
```

## Vérification après Déploiement

### 1. Vérifier les logs CORS sur Render

Après le déploiement, vérifiez dans les logs de Render que vous voyez :

```
CORS Configuration - Allowed origins: [http://localhost:4200, https://votre-frontend.com, ...]
```

### 2. Tester depuis le frontend

1. Démarrez votre frontend en développement :
   ```bash
   cd sendbyopweb-user-final
   ng serve
   ```

2. Ouvrez la console du navigateur (F12)

3. Testez une requête (ex: liste des vols)

4. Vérifiez qu'il n'y a **plus** d'erreurs CORS

### 3. Tests à effectuer

- [ ] Chargement de la liste des vols publics
- [ ] Authentification (login)
- [ ] Création d'une réservation
- [ ] Initiation d'un paiement

## Architecture des Dates

### BaseEntity (java.util.Date)
```java
@CreationTimestamp
private Date createdAt;

@UpdateTimestamp
private Date updatedAt;
```

### Entités spécifiques (LocalDateTime)
```java
// Transaction.java
private LocalDateTime completedAt;
private LocalDateTime webhookReceivedAt;
```

### Conversion automatique
```java
// Utiliser DateTimeUtils pour les conversions
LocalDateTime ldt = DateTimeUtils.dateToLocalDateTime(date);
Date date = DateTimeUtils.localDateTimeToDate(ldt);
```

## Notes Importantes

1. **BaseEntity utilise Date** : Les champs `createdAt` et `updatedAt` sont automatiquement gérés par Hibernate avec `@CreationTimestamp` et `@UpdateTimestamp`. Ne les modifiez pas manuellement.

2. **DTOs utilisent LocalDateTime** : Pour une meilleure compatibilité avec les API REST modernes et les clients JavaScript.

3. **Conversions automatiques** : DateTimeUtils gère les conversions en préservant le fuseau horaire système.

## En cas de problème

### Erreur CORS persiste

1. Vérifiez que la variable `CORS_ALLOWED_ORIGINS` est bien définie sur Render
2. Vérifiez qu'elle contient l'URL exacte de votre frontend
3. Redémarrez l'application backend sur Render
4. Vérifiez les logs pour voir les origines autorisées

### Erreur de compilation

1. Assurez-vous que `DateTimeUtils.java` est présent
2. Faites un clean build :
   ```bash
   mvn clean compile
   ```

### Frontend ne se connecte pas

1. Vérifiez l'URL dans `environment.ts` (doit inclure `/api/v1`)
2. Testez l'URL directement dans le navigateur (ajoutez `/actuator/health` à la fin)
3. Vérifiez que le backend est bien démarré sur Render

## Prochaines Étapes

1. ✅ Résolu : Problèmes CORS
2. ✅ Résolu : Erreurs de compilation Date/LocalDateTime
3. ✅ Résolu : URL frontend incorrecte
4. ⏭️ Déployer le backend sur Render
5. ⏭️ Tester l'application complète
6. ⏭️ Déployer le frontend sur un hébergeur (Vercel, Netlify, etc.)
