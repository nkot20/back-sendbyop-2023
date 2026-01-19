# Configuration SendGrid - Guide Rapide

## ✅ Configuration actuelle

Le backend est maintenant configuré pour utiliser SendGrid avec ces paramètres :

```properties
Host: smtp.sendgrid.net
Port: 587
Username: apikey
Password: ${SENDGRID}  # Votre clé API SendGrid
```

## 🚀 Étapes pour activer SendGrid

### 1️⃣ Obtenir votre clé API SendGrid

Si vous n'avez pas encore de clé API :

1. **Connexion SendGrid** : [https://app.sendgrid.com](https://app.sendgrid.com)
2. **Settings** → **API Keys**
3. **Create API Key**
   - Name: `SendByOp Production`
   - Permissions: **Full Access** (ou **Mail Send** minimum)
4. **Copiez la clé** immédiatement (vous ne pourrez plus la voir !)

### 2️⃣ Vérifier votre adresse email expéditrice

⚠️ **IMPORTANT** : SendGrid exige que vous vérifiiez l'adresse FROM avant d'envoyer des emails.

1. **Settings** → **Sender Authentication**
2. **Verify a Single Sender**
3. Remplissez le formulaire :
   - **From Email Address** : `noreply@sendbyop.com` (ou votre domaine)
   - **Reply To** : votre email de support
   - **Company Name** : SendByOp
   - etc.
4. **Vérifiez via l'email** reçu

### 3️⃣ Configurer les variables d'environnement

#### Sur Render :

1. Allez dans votre service Backend sur Render
2. **Environment** → **Environment Variables**
3. Ajoutez/Modifiez ces variables :

```bash
SENDGRID=<VOTRE_CLÉ_API_SENDGRID>
EMAIL_FROM=noreply@sendbyop.com
```

⚠️ **Note** : `EMAIL_FROM` doit correspondre à l'email que vous avez vérifié dans SendGrid.

#### En local (.env) :

Votre fichier `.env` devrait déjà contenir :

```bash
SENDGRID=SG.xxxxxxxxxxxxxxxxxxxxx
EMAIL_FROM=noreply@sendbyop.com
```

### 4️⃣ Redéployer l'application

1. **Commit et push** vos changements :
   ```bash
   git add .
   git commit -m "Configure SendGrid email service"
   git push
   ```

2. Render va automatiquement redéployer

### 5️⃣ Tester l'envoi d'emails

#### Test 1 : Créer un nouveau compte

1. Allez sur votre application
2. Créez un nouveau compte utilisateur
3. Vérifiez que vous recevez l'email de vérification

#### Test 2 : Vérifier les logs Render

Dans les logs Render, vous devriez voir :

```
✅ Envoi d'un email HTML à user@example.com
✅ Email envoyé avec succès
```

Au lieu de :
```
❌ Mail server connection failed
❌ Connection timed out
```

#### Test 3 : Dashboard SendGrid

1. Allez dans **Activity** sur SendGrid
2. Vous devriez voir vos emails avec le statut :
   - **Processed** : Reçu par SendGrid
   - **Delivered** : Livré au destinataire
   - **Opened** : Ouvert par le destinataire (si tracking activé)

## 🔍 Débogage

### Problème : "550 The from address does not match a verified Sender Identity"

**Solution :** L'email FROM n'est pas vérifié dans SendGrid.

1. Vérifiez votre Sender Identity dans SendGrid
2. Assurez-vous que `EMAIL_FROM` correspond exactement

### Problème : "Connection timed out" persiste

**Solution :** La variable `SENDGRID` n'est pas correctement configurée.

1. Vérifiez que la variable existe sur Render
2. Vérifiez qu'il n'y a pas d'espaces avant/après la clé
3. Redémarrez le service manuellement

### Problème : "Authentication failed"

**Solution :** La clé API est invalide ou a des permissions insuffisantes.

1. Régénérez une nouvelle clé API dans SendGrid
2. Assurez-vous qu'elle a les permissions **Mail Send**
3. Mettez à jour `SENDGRID` sur Render

## 📊 Limites du plan gratuit

- ✅ **100 emails/jour** gratuitement
- ✅ Suffisant pour la plupart des projets en démarrage
- ✅ Possibilité de passer à un plan payant si besoin

Si vous atteignez la limite :
- Plan **Essentials** : 19,95$/mois pour 50 000 emails
- Plan **Pro** : 89,95$/mois pour 1,5M emails

## 🎯 Checklist finale

Avant de dire que c'est terminé, vérifiez :

- [ ] Clé API SendGrid créée
- [ ] Sender Identity vérifiée (email FROM confirmé)
- [ ] Variable `SENDGRID` configurée sur Render
- [ ] Variable `EMAIL_FROM` configurée sur Render
- [ ] Application redéployée
- [ ] Email de test envoyé et reçu
- [ ] Logs Render ne montrent plus d'erreurs "Connection timed out"
- [ ] Dashboard SendGrid montre les emails comme "Delivered"

## ✅ C'est tout !

Une fois ces étapes complétées, vos emails seront envoyés de manière fiable via SendGrid, même en production sur Render ! 🎉

---

**Besoin d'aide ?**
- [Documentation SendGrid](https://docs.sendgrid.com/)
- [SendGrid Support](https://support.sendgrid.com/)
