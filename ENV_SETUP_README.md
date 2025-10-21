# 🚀 Configuration Rapide des Variables d'Environnement

## 📋 Démarrage Rapide (5 minutes)

### Étape 1 : Générer un JWT Secret

**Windows (PowerShell) :**
```powershell
.\generate-jwt-secret.ps1
```

**Linux/Mac (Bash) :**
```bash
chmod +x generate-jwt-secret.sh
./generate-jwt-secret.sh
```

**Ou manuellement :**
```bash
# Linux/Mac
openssl rand -base64 64 | tr -d '\n'

# Windows PowerShell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

### Étape 2 : Créer le fichier .env

```bash
# Copier le fichier exemple
cp .env.example .env
```

### Étape 3 : Éditer le .env

Ouvrez `.env` et remplissez les valeurs :

```properties
# JWT (OBLIGATOIRE)
JWT_SECRET=COLLEZ_VOTRE_SECRET_ICI
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Base de données (OBLIGATOIRE)
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sendbyop
DB_USERNAME=root
DB_PASSWORD=votre_mot_de_passe

# Email (OBLIGATOIRE pour vérification d'email)
EMAIL_USERNAME=votre-email@gmail.com
EMAIL_PASSWORD=votre-mot-de-passe-application

# Autres (OPTIONNEL)
TWILIO_ACCOUNT_SID=votre_sid
TWILIO_AUTH_TOKEN=votre_token
```

### Étape 4 : Démarrer l'Application

```bash
mvn spring-boot:run
```

## 📁 Fichiers Créés

| Fichier | Description | À Commiter ? |
|---------|-------------|--------------|
| `.env.example` | Template avec toutes les variables | ✅ Oui |
| `.env` | Vos vraies valeurs | ❌ NON ! |
| `generate-jwt-secret.ps1` | Script Windows | ✅ Oui |
| `generate-jwt-secret.sh` | Script Linux/Mac | ✅ Oui |
| `JWT_SETUP_GUIDE.md` | Guide complet JWT | ✅ Oui |

## 🔐 Variables Obligatoires

### Pour Démarrer l'Application

```properties
JWT_SECRET=...
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
DB_HOST=localhost
DB_NAME=sendbyop
DB_USERNAME=root
DB_PASSWORD=...
```

### Pour l'Envoi d'Emails

```properties
EMAIL_USERNAME=...
EMAIL_PASSWORD=...
```

### Pour les SMS (Twilio)

```properties
TWILIO_ACCOUNT_SID=...
TWILIO_AUTH_TOKEN=...
TWILIO_PHONE_NUMBER=...
```

## 🎯 Configuration par Environnement

### Développement (.env)

```properties
JWT_SECRET=dev_secret_ici
JWT_EXPIRATION=86400000        # 24h (pratique pour dev)
BASE_URL=http://localhost:8080
```

### Production (.env.prod)

```properties
JWT_SECRET=prod_secret_different_ici
JWT_EXPIRATION=900000          # 15min (plus sécurisé)
BASE_URL=https://api.sendbyop.com
```

## 🔧 Configuration Email Gmail

### 1. Activer l'Authentification à 2 Facteurs

1. Allez sur https://myaccount.google.com/security
2. Activez "Validation en deux étapes"

### 2. Générer un Mot de Passe d'Application

1. Allez sur https://myaccount.google.com/apppasswords
2. Sélectionnez "Autre (nom personnalisé)"
3. Entrez "SendByOp"
4. Copiez le mot de passe généré (16 caractères)

### 3. Configurer le .env

```properties
EMAIL_USERNAME=votre-email@gmail.com
EMAIL_PASSWORD=abcd efgh ijkl mnop  # Le mot de passe d'application
```

## 🐛 Dépannage

### Erreur : "JWT secret not configured"

**Solution :**
```bash
# Vérifier que JWT_SECRET est dans .env
cat .env | grep JWT_SECRET

# Si absent, l'ajouter
echo "JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')" >> .env
```

### Erreur : "Could not connect to database"

**Solution :**
```bash
# Vérifier que MySQL/PostgreSQL est démarré
# Windows
net start MySQL80

# Linux
sudo systemctl start mysql

# Mac
brew services start mysql
```

### Erreur : "Failed to send email"

**Solution :**
1. Vérifier `EMAIL_USERNAME` et `EMAIL_PASSWORD`
2. Vérifier que le mot de passe d'application Gmail est correct
3. Vérifier que l'authentification à 2 facteurs est activée

## 📚 Documentation Complète

- **JWT_SETUP_GUIDE.md** - Guide complet JWT
- **EMAIL_VERIFICATION_GUIDE.md** - Système de vérification d'email
- **EMAIL_TEMPLATING_GUIDE.md** - Templates d'emails
- **ENCRYPTION_SETUP.md** - Chiffrement des données bancaires

## ✅ Checklist de Configuration

- [ ] Fichier `.env` créé
- [ ] JWT_SECRET généré et configuré
- [ ] Base de données configurée
- [ ] Email configuré (si nécessaire)
- [ ] `.env` ajouté à `.gitignore`
- [ ] Application démarre sans erreur
- [ ] Test d'inscription fonctionne
- [ ] Test de connexion fonctionne
- [ ] Email de vérification reçu

## 🚨 Sécurité - À NE JAMAIS FAIRE

❌ Commiter le fichier `.env` dans Git
❌ Partager vos secrets publiquement
❌ Utiliser le même secret pour dev et prod
❌ Hardcoder les secrets dans le code
❌ Utiliser des secrets faibles ou courts

## ✅ Sécurité - Bonnes Pratiques

✅ Utiliser des secrets différents par environnement
✅ Générer des secrets forts (64+ caractères)
✅ Changer les secrets régulièrement (tous les 3-6 mois)
✅ Utiliser un gestionnaire de secrets en production
✅ Activer l'authentification à 2 facteurs

---

**Besoin d'aide ?** Consultez les guides détaillés ou contactez l'équipe technique.
