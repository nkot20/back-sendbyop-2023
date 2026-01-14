# 📋 Règles Métier - SendByOp Platform

## 🔄 Tâches Planifiées (CRON Jobs)

### 1. Expiration Automatique des Vols ⏰
**Fréquence**: Toutes les heures (à la minute 0)  
**Fonctionnement**: Marque automatiquement les vols en statut `EXPIRED` après leur date/heure d'arrivée.  
**Règle**: Aucune nouvelle réservation ne peut être créée sur un vol expiré.

```
Cron: 0 0 * * * * (toutes les heures)
Statut: ACTIVE → EXPIRED
Condition: arrivalDate < maintenant
```

### 2. Confirmation Automatique de Réception 📦
**Fréquence**: Toutes les 6 heures  
**Délai configurable**: 72h par défaut  
**Fonctionnement**: Si le destinataire ne confirme pas la réception dans les 72h après marquage "Livré" par le voyageur, le système présume la bonne réception et débloque le montant au voyageur.

```
Cron: 0 0 */6 * * * (toutes les 6 heures)
Statut: PARCEL_DELIVERED_TO_RECEIVER → CONFIRMED_BY_RECEIVER
Délai: paramétrable via platform_settings.reception_confirmation_hours
```

### 3. Fermeture des Périodes d'Avis ⭐
**Fréquence**: Tous les jours à minuit  
**Délai configurable**: 90 jours par défaut  
**Fonctionnement**: Ferme la possibilité de laisser un avis 90 jours après confirmation de réception.

```
Cron: 0 0 0 * * * (tous les jours à minuit)
Condition: CONFIRMED_BY_RECEIVER depuis plus de 90 jours
Délai: paramétrable via platform_settings.review_deadline_days
```

### 4. Annulation des Réservations Impayées 💳
**Fréquence**: Toutes les 30 minutes  
**Délai configurable**: 12h par défaut  
**Fonctionnement**: Annule automatiquement les réservations confirmées mais non payées après expiration du délai.

```
Cron: 0 */30 * * * * (toutes les 30 minutes)
Statut: CONFIRMED_UNPAID → CANCELLED_PAYMENT_TIMEOUT
Délai: paramétrable via platform_settings.payment_timeout_hours
```

---

## 💰 Règles de Calcul Financier

### Commission Plateforme
- **Taux par défaut**: 15% (configurable)
- **Calcul**: `Commission = Montant Total × 15%`
- **Paramètre**: `platform_settings.commission_percentage`

### Gain Net du Voyageur
```
Gain Net = Montant Total - Commission - Assurance
Exemple: 177,50 EUR - 26,63 EUR - 5 EUR = 145,87 EUR
```

### TVA/Taxes
- **Europe**: 20% (configurable)
- **Paramètre**: `platform_settings.vat_rate_europe`
- **Affichage**: Clairement indiqué dans le récapitulatif de paiement

### Assurance
- **Montant fixe par défaut**: 5 EUR
- **Paramètre**: `platform_settings.insurance_amount`

---

## 🚫 Règles d'Annulation et Remboursement

### Avant Paiement
- **Annulation**: Gratuite (100%)
- **Remboursement**: Aucun montant n'a été débité
- **Statut**: PENDING_CONFIRMATION ou CONFIRMED_UNPAID

### Après Paiement - Plus de 4h avant le vol
- **Remboursement**: 90% du montant payé (configurable)
- **Assurance**: Remboursée
- **Paramètres**:
  - `platform_settings.refund_rate_before_deadline` (90%)
  - `platform_settings.critical_cancellation_hours` (4h)

### Après Paiement - Moins de 4h avant le vol
- **Annulation**: ❌ IMPOSSIBLE
- **Remboursement**: 0%
- **Raison**: Délai critique dépassé

---

## 🔒 Séquestre (Escrow)

Le montant payé par le client reste **gelé** chez SendByOp jusqu'à confirmation de réception par le destinataire.

**Déblocage automatique**:
- Confirmation manuelle du destinataire ✅
- OU après 72h sans contestation (présomption de bonne réception) ⏰

**Protection acheteur**: Garantie que le voyageur ne reçoit le paiement qu'après livraison confirmée.

---

## 💸 Règles de Reversement

### Seuil Minimum
- **Montant minimum**: 50 EUR (configurable)
- **Paramètre**: `platform_settings.minimum_payout_amount`
- Le voyageur doit atteindre ce seuil pour demander un virement

### Frais de Virement
- **Frais couverts par SendByOp**: Jusqu'à 5 EUR
- **Au-delà de 5 EUR**: Frais à charge du voyageur
- **Paramètre**: `platform_settings.transfer_fee_covered`

---

## ⚙️ Configuration des Paramètres

Tous les paramètres sont stockés dans la table `platform_settings` et peuvent être modifiés via l'interface admin.

### Table de Configuration

| Paramètre | Valeur par défaut | Description |
|-----------|-------------------|-------------|
| `commission_percentage` | 15.00% | Commission plateforme |
| `reception_confirmation_hours` | 72h | Délai confirmation réception |
| `review_deadline_days` | 90 jours | Délai pour laisser un avis |
| `minimum_payout_amount` | 50.00 EUR | Seuil minimum de reversement |
| `transfer_fee_covered` | 5.00 EUR | Frais de virement couverts |
| `refund_rate_before_deadline` | 90.00% | Taux de remboursement avant délai critique |
| `critical_cancellation_hours` | 4h | Délai critique avant vol |
| `vat_rate_europe` | 20.00% | Taux de TVA en Europe |
| `insurance_amount` | 5.00 EUR | Montant de l'assurance |
| `payment_timeout_hours` | 12h | Délai de paiement |

---

## 🔗 API Endpoints

### Calcul de Remboursement
```http
GET /api/v1/cancellation/calculate/{bookingId}
Authorization: Bearer <token>
```
**Réponse**:
```json
{
  "canCancel": true,
  "refundAmount": 159.75,
  "refundPercentage": 90.00,
  "insuranceRefund": 5.00,
  "totalRefund": 164.75,
  "amountPaid": 177.50,
  "reason": "Remboursement de 90% car annulation plus de 4 heures avant le vol"
}
```

### Annulation de Réservation
```http
POST /api/v1/cancellation/cancel/{bookingId}
Authorization: Bearer <token>
```

### Calcul des Gains Voyageur
```http
GET /api/v1/cancellation/earnings?totalAmount=177.50
Authorization: Bearer <token>
```
**Réponse**:
```json
{
  "totalAmount": 177.50,
  "commission": 26.63,
  "commissionPercentage": 15.00,
  "insurance": 5.00,
  "netEarnings": 145.87
}
```

---

## 🚀 Activation

Les tâches planifiées sont **automatiquement activées** au démarrage de l'application grâce à l'annotation `@EnableScheduling`.

Pour **désactiver temporairement** les cron jobs, commenter `@EnableScheduling` dans `ExpeditionApplication.java`.

---

## 📊 Monitoring et Logs

Tous les cron jobs génèrent des logs détaillés avec le préfixe `=== CRON: ...`

**Exemple de logs**:
```
2026-01-13 00:00:00 INFO  === CRON: Vérification des vols expirés ===
2026-01-13 00:00:01 INFO  Vol 123 marqué comme EXPIRED (arrivée: 2026-01-12 22:00:00)
2026-01-13 00:00:02 INFO  === 3 vol(s) marqué(s) comme EXPIRED ===
```

---

## 🧪 Tests

Pour tester les règles métier:

1. **Expiration de vol**: Créer un vol avec une date d'arrivée passée, attendre 1h
2. **Confirmation auto**: Marquer un colis comme livré, attendre 72h
3. **Annulation**: Essayer d'annuler une réservation à différents moments (avant paiement, après paiement, proche du vol)
4. **Calcul gains**: Utiliser l'endpoint `/earnings` avec différents montants

---

## 📝 Notes Importantes

⚠️ **Attention**: 
- Les cron jobs utilisent le fuseau horaire du serveur
- Assurez-vous que l'horloge système est correctement configurée
- Les paramètres de `platform_settings` doivent être initialisés lors du déploiement

✅ **Bonnes pratiques**:
- Monitorer les logs des cron jobs régulièrement
- Ajuster les paramètres selon les besoins métier
- Tester en environnement de staging avant production
