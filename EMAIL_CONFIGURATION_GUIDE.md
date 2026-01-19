# Guide de Configuration Email pour Production

## Problème

Sur Render (et la plupart des hébergeurs cloud), les connexions SMTP sortantes vers Gmail sont **bloquées** ou **timeout**, car :
- Les ports SMTP (25, 587, 465) sont souvent bloqués pour prévenir le spam
- Gmail bloque les connexions depuis des IPs de serveurs cloud
- Les hébergeurs cloud imposent des restrictions réseau

## Solutions Recommandées

### ✅ Solution 1 : SendGrid (RECOMMANDÉ)

**Avantages :**
- ✅ Gratuit jusqu'à 100 emails/jour
- ✅ Fiable et rapide
- ✅ Excellente délivrabilité
- ✅ Dashboard pour suivre les emails
- ✅ Fonctionne parfaitement avec Render

**Configuration :**

#### 1. Créer un compte SendGrid

1. Allez sur [https://sendgrid.com](https://sendgrid.com)
2. Créez un compte gratuit
3. Vérifiez votre email

#### 2. Générer une API Key

1. Dans SendGrid, allez dans **Settings** → **API Keys**
2. Cliquez sur **Create API Key**
3. Nom : `SendByOp Production`
4. Permissions : **Full Access** (ou **Mail Send** seulement)
5. Copiez la clé API (vous ne pourrez plus la voir après)

#### 3. Vérifier un expéditeur (Sender Identity)

SendGrid exige que vous vérifiiez l'adresse email d'expédition :

1. Allez dans **Settings** → **Sender Authentication**
2. **Option A - Single Sender Verification** (rapide) :
   - Cliquez sur **Verify a Single Sender**
   - Remplissez le formulaire avec votre email
   - Confirmez via l'email reçu

3. **Option B - Domain Authentication** (professionnel) :
   - Si vous avez un domaine personnalisé
   - Suivez les instructions pour configurer les DNS

#### 4. Configurer les variables d'environnement sur Render

Dans votre service Render, ajoutez ces variables :

```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=<VOTRE_API_KEY_SENDGRID>
EMAIL_FROM=noreply@sendbyop.com
```

⚠️ **Important :** 
- `MAIL_USERNAME` doit être exactement `apikey` (c'est le username standard de SendGrid)
- `MAIL_PASSWORD` est votre clé API SendGrid
- `EMAIL_FROM` doit correspondre à l'adresse vérifiée dans SendGrid

#### 5. Redéployer sur Render

Après avoir ajouté les variables, redéployez l'application.

---

### ✅ Solution 2 : Mailgun

**Avantages :**
- Gratuit jusqu'à 5 000 emails/mois (les 3 premiers mois)
- Bon pour l'Europe

**Configuration :**

1. Créez un compte sur [mailgun.com](https://mailgun.com)
2. Vérifiez votre domaine
3. Obtenez vos identifiants SMTP

Variables d'environnement :
```bash
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=<votre_username_mailgun>
MAIL_PASSWORD=<votre_password_mailgun>
EMAIL_FROM=noreply@votre-domaine.com
```

---

### ✅ Solution 3 : AWS SES (Amazon Simple Email Service)

**Avantages :**
- Très peu cher (0,10$ pour 1000 emails)
- Excellente intégration AWS
- Très fiable

**Configuration :**

1. Créez un compte AWS
2. Activez SES dans votre région
3. Vérifiez votre email/domaine
4. Créez des credentials SMTP

Variables d'environnement :
```bash
MAIL_HOST=email-smtp.eu-west-1.amazonaws.com  # Selon votre région
MAIL_PORT=587
MAIL_USERNAME=<votre_smtp_username>
MAIL_PASSWORD=<votre_smtp_password>
EMAIL_FROM=noreply@votre-domaine.com
```

---

### ⚠️ Solution 4 : Gmail avec App Password (Déconseillé pour production)

Si vous voulez absolument utiliser Gmail :

**Configuration :**

1. Activez la vérification en 2 étapes sur votre compte Gmail
2. Générez un "App Password" :
   - Allez dans **Sécurité** → **Validation en deux étapes** → **Mots de passe des applications**
   - Créez un nouveau mot de passe pour "Autre (nom personnalisé)"

Variables d'environnement :
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<votre_email@gmail.com>
MAIL_PASSWORD=<votre_app_password>  # PAS votre mot de passe Gmail normal
EMAIL_FROM=<votre_email@gmail.com>
```

⚠️ **Problème :** Render peut quand même bloquer les connexions à Gmail. **Non recommandé pour production.**

---

## Configuration Actuelle du Projet

Le projet a été configuré pour supporter **SendGrid par défaut** en production avec fallback vers d'autres providers.

### Fichiers modifiés :

**application-prod.properties :**
```properties
spring.mail.host=${MAIL_HOST:smtp.sendgrid.net}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:apikey}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=${MAIL_HOST:smtp.sendgrid.net}
email.from=${EMAIL_FROM:noreply@sendbyop.com}
```

### Variables d'environnement requises sur Render :

| Variable | Valeur pour SendGrid | Description |
|----------|---------------------|-------------|
| `MAIL_HOST` | `smtp.sendgrid.net` | Serveur SMTP |
| `MAIL_PORT` | `587` | Port SMTP |
| `MAIL_USERNAME` | `apikey` | Username SendGrid (toujours "apikey") |
| `MAIL_PASSWORD` | `<votre_api_key>` | Votre clé API SendGrid |
| `EMAIL_FROM` | `noreply@sendbyop.com` | Email expéditeur (vérifié dans SendGrid) |

---

## Test après Configuration

### 1. Vérifier les logs Render

Après redéploiement, vérifiez les logs pour :
```
✅ Connection to smtp.sendgrid.net:587 successful
✅ Email sent successfully to etiennenkot1@gmail.com
```

### 2. Tester l'envoi d'email

1. Créez un nouveau compte utilisateur
2. Vérifiez que l'email de vérification arrive
3. Vérifiez dans le dashboard SendGrid :
   - **Activity** → Voir les emails envoyés
   - Statut : Delivered, Opened, etc.

### 3. Déboguer si problème

Si l'envoi échoue encore :

1. **Vérifiez les variables d'environnement** sur Render
2. **Vérifiez que l'email FROM est vérifié** dans SendGrid
3. **Consultez les logs SendGrid** pour voir les erreurs
4. **Testez la connexion SMTP** :
   ```bash
   telnet smtp.sendgrid.net 587
   ```

---

## Comparaison des Services

| Service | Gratuit | Payant | Délivrabilité | Facilité | Recommandé |
|---------|---------|--------|---------------|----------|------------|
| **SendGrid** | 100/jour | 19,95$/mois | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ OUI |
| **Mailgun** | 5000/mois (3 mois) | 35$/mois | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ OUI |
| **AWS SES** | 62000/mois (AWS Free Tier) | 0,10$/1000 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⚠️ Plus complexe |
| **Gmail** | Limité | - | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ❌ NON (Dev uniquement) |

---

## Conclusion

Pour **SendByOp en production sur Render**, la meilleure solution est :

🎯 **SendGrid** avec le plan gratuit (100 emails/jour)

**Prochaines étapes :**
1. ✅ Créer compte SendGrid
2. ✅ Générer API Key
3. ✅ Vérifier l'adresse email d'expédition
4. ✅ Configurer les variables sur Render
5. ✅ Redéployer l'application
6. ✅ Tester l'envoi d'emails

Si vous avez des questions ou des problèmes, consultez :
- [Documentation SendGrid Spring Boot](https://docs.sendgrid.com/for-developers/sending-email/spring-boot)
- [Render Documentation](https://render.com/docs)
