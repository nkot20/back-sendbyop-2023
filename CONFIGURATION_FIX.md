# 🔧 Corrections de Configuration - SendByOp

## ✅ Problèmes Résolus

### 1. Configuration JWT Incorrecte

#### Problème
L'application ne démarrait pas avec l'erreur :
```
For input string: "1000à="
Failed to convert value of type 'java.lang.String' to required type 'int'
```

#### Cause
- Les fichiers de configuration utilisaient `grokonez.app.jwtExpiration` 
- Le code Java attendait `app.jwt.expiration`
- Incompatibilité entre les noms de propriétés

#### Solution Appliquée

**Fichiers modifiés :**
- `application-dev.properties`
- `application-prod.properties`

**Avant :**
```properties
grokonez.app.jwtSecret=${JWT_SECRET}
grokonez.app.jwtExpiration=20400
```

**Après :**
```properties
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400
app.jwt.refreshExpiration=604800
```

### 2. Unité de Temps Clarifiée

#### Important : Secondes vs Millisecondes

Le code Java multiplie la valeur par 1000 :
```java
.setExpiration(new Date((new Date()).getTime() + jwtExpiration * 1000))
```

**Donc les valeurs dans les properties sont en SECONDES, pas en millisecondes !**

| Configuration | Valeur | Durée Réelle |
|---------------|--------|--------------|
| `app.jwt.expiration=86400` | 86400 secondes | 24 heures |
| `app.jwt.expiration=900` | 900 secondes | 15 minutes |
| `app.jwt.refreshExpiration=604800` | 604800 secondes | 7 jours |

### 3. Configuration par Environnement

#### Développement (`application-dev.properties`)
```properties
app.jwt.expiration=86400          # 24 heures (pratique pour dev)
app.jwt.refreshExpiration=604800  # 7 jours
```

#### Production (`application-prod.properties`)
```properties
app.jwt.expiration=900            # 15 minutes (plus sécurisé)
app.jwt.refreshExpiration=604800  # 7 jours
```

## 📋 Propriétés JWT Complètes

### Dans application.properties ou application-{profile}.properties

```properties
# JWT Configuration
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400
app.jwt.refreshExpiration=604800
```

### Dans .env (Variables d'Environnement)

```properties
JWT_SECRET=votre_secret_genere_ici
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800
```

## 🔍 Mapping des Propriétés

| Fichier Properties | Variable Env | Code Java | Type |
|-------------------|--------------|-----------|------|
| `app.jwt.secret` | `JWT_SECRET` | `jwtSecret` | String |
| `app.jwt.expiration` | `JWT_EXPIRATION` | `jwtExpiration` | int (secondes) |
| `app.jwt.refreshExpiration` | `JWT_REFRESH_EXPIRATION` | `refreshTokenExpiration` | int (secondes) |

## 🎯 Valeurs Recommandées

### Développement
```properties
app.jwt.expiration=86400          # 24h - Pratique pour dev
app.jwt.refreshExpiration=2592000 # 30 jours
```

### Staging
```properties
app.jwt.expiration=3600           # 1h - Test réaliste
app.jwt.refreshExpiration=604800  # 7 jours
```

### Production
```properties
app.jwt.expiration=900            # 15min - Très sécurisé
app.jwt.refreshExpiration=604800  # 7 jours
```

## 🐛 Dépannage

### Erreur : "For input string: XXX"

**Cause :** Valeur non numérique dans la configuration

**Solution :**
1. Vérifier que `app.jwt.expiration` contient uniquement des chiffres
2. Pas d'espaces, pas de caractères spéciaux
3. Valeur en secondes (pas de millisecondes)

### Erreur : "JWT expired"

**Cause :** Token expiré trop rapidement

**Solution :**
1. Augmenter `app.jwt.expiration` (en secondes)
2. Implémenter le refresh token
3. Vérifier l'horloge du serveur

### Erreur : "Invalid JWT signature"

**Cause :** Secret JWT incorrect ou changé

**Solution :**
1. Vérifier que `JWT_SECRET` est défini dans `.env`
2. Vérifier que le secret n'a pas changé
3. Régénérer un nouveau secret si nécessaire

## 📝 Checklist de Configuration

- [ ] Fichier `.env` créé avec `JWT_SECRET`
- [ ] `app.jwt.expiration` en secondes (pas millisecondes)
- [ ] `app.jwt.refreshExpiration` configuré
- [ ] Valeurs différentes pour dev/prod
- [ ] Application démarre sans erreur
- [ ] Test de connexion fonctionne
- [ ] Token expire au bon moment

## 🔗 Fichiers Concernés

### Configuration
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`
- `.env` (non commité)
- `.env.example` (template)

### Code Java
- `src/main/java/com/sendByOP/expedition/security/jwt/JwtProvider.java`

### Documentation
- `JWT_SETUP_GUIDE.md` - Guide complet JWT
- `ENV_SETUP_README.md` - Configuration environnement
- `.env.example` - Template de configuration

## ✅ Vérification

Pour vérifier que tout fonctionne :

```bash
# 1. Démarrer l'application
mvn spring-boot:run

# 2. Tester l'authentification
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'

# 3. Vérifier le token reçu sur https://jwt.io
# - Algorithme doit être HS512
# - exp (expiration) doit correspondre à votre configuration
```

---

**Date de correction** : 2024-10-11
**Status** : ✅ Résolu
