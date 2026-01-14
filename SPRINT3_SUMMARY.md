# Sprint 3: Confirmation, Rejet & Paiement (TDD) ✅

**Date:** 23 octobre 2025  
**Durée:** 2 heures  
**Statut:** ✅ COMPLÉTÉ

---

## 🎯 Objectifs

Implémenter les 3 opérations critiques du cycle de vie d'une réservation:
1. **Confirmation** par le voyageur
2. **Rejet** par le voyageur  
3. **Paiement** par le client

Approche **Test-Driven Development (TDD)**

---

## ✅ Composants Créés

### 1. PaymentRequest.java (DTO)
**Fichier:** `src/main/java/.../models/dto/PaymentRequest.java`

**Champs:**
- `amount` - Montant du paiement (@NotNull, @DecimalMin)
- `paymentMethod` - Méthode (CREDIT_CARD, PAYPAL, etc.)
- `cardNumber`, `cardHolderName`, `cardExpiryMonth/Year`, `cardCvv` - Infos carte
- `paymentToken` - Token Stripe/PayPal
- `externalTransactionId` - ID transaction externe
- `notes` - Notes additionnelles

---

### 2. IBookingService.java (Interface enrichie)
**Fichier:** `src/main/java/.../services/iServices/IBookingService.java`

**3 nouvelles méthodes:**

#### confirmBooking(Integer bookingId, Integer travelerId)
```
PENDING_CONFIRMATION → CONFIRMED_UNPAID
+ Définit deadline paiement (selon PlatformSettings)
+ Enregistre date confirmation
+ Validation propriétaire vol
```

#### rejectBooking(Integer bookingId, Integer travelerId, String reason)
```
PENDING_CONFIRMATION → CANCELLED_BY_TRAVELER
+ Enregistre raison rejet
+ Validation propriétaire vol
```

#### processPayment(Integer bookingId, PaymentRequest, Integer customerId)
```
CONFIRMED_UNPAID → CONFIRMED_PAID
+ Vérifie deadline non dépassée
+ Valide montant = totalPrice
+ Validation propriétaire réservation
```

---

### 3. BookingServiceTest.java (9 nouveaux tests)
**Fichier:** `src/test/java/.../services/BookingServiceTest.java`

**Tests Sprint 3 (14-22):**

| # | Test | Description |
|---|------|-------------|
| 14 | `shouldConfirmBookingByTraveler` | Confirmation réussie |
| 15 | `shouldRejectConfirmationByNonOwner` | Non-propriétaire rejeté |
| 16 | `shouldRejectConfirmationWhenInvalidStatus` | Statut invalide |
| 17 | `shouldRejectBookingByTraveler` | Rejet réussi |
| 18 | `shouldRejectRejectionWhenInvalidStatus` | Statut invalide pour rejet |
| 19 | `shouldProcessPaymentForConfirmedBooking` | Paiement réussi |
| 20 | `shouldRejectPaymentWithIncorrectAmount` | Montant incorrect |
| 21 | `shouldRejectPaymentWhenInvalidStatus` | Statut invalide pour paiement |
| 22 | `shouldRejectPaymentByNonOwner` | Non-propriétaire rejeté |

**Total tests BookingService:** 22 (13 Sprint 2c + 9 Sprint 3)

---

### 4. ErrorInfo.java (2 nouveaux codes)
**Fichier:** `src/main/java/.../exception/ErrorInfo.java`

**Ajoutés:**
- `UNAUTHORIZED` - Non autorisé (403)
- `INVALID_STATUS` - Statut invalide pour opération (400)

---

### 5. BookingService.java (3 méthodes implémentées)
**Fichier:** `src/main/java/.../services/impl/BookingService.java`

#### confirmBooking()
```java
✅ Vérification réservation existe
✅ Validation voyageur = propriétaire vol
✅ Validation statut = PENDING_CONFIRMATION
✅ Change statut → CONFIRMED_UNPAID
✅ Calcul deadline paiement (PlatformSettings)
✅ Enregistre date confirmation
✅ Logging complet
```

#### rejectBooking()
```java
✅ Vérification réservation existe
✅ Validation voyageur = propriétaire vol
✅ Validation statut = PENDING_CONFIRMATION
✅ Change statut → CANCELLED_BY_TRAVELER
✅ Enregistre raison rejet
✅ Logging complet
```

#### processPayment()
```java
✅ Vérification réservation existe
✅ Validation client = propriétaire réservation
✅ Validation statut = CONFIRMED_UNPAID
✅ Vérification deadline non dépassée
✅ Validation montant = totalPrice
✅ [Future] Intégration gateway paiement
✅ Change statut → CONFIRMED_PAID
✅ Logging complet
```

#### buildBookingResponse() (méthode helper privée)
```java
✅ Construction BookingResponseDto
✅ Réutilisable par toutes les méthodes
✅ Code DRY (Don't Repeat Yourself)
```

---

### 6. BookingController.java (3 nouveaux endpoints)
**Fichier:** `src/main/java/.../controllers/BookingController.java`

#### PUT /api/bookings/{bookingId}/confirm
```
Confirme une réservation (voyageur)
Params: travelerId (query)
Security: @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
Response: 200 OK + BookingResponseDto
```

#### PUT /api/bookings/{bookingId}/reject
```
Rejette une réservation (voyageur)
Params: travelerId (query), reason (query, optionnel)
Security: @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
Response: 200 OK + BookingResponseDto
```

#### POST /api/bookings/{bookingId}/payment
```
Traite le paiement (client)
Body: PaymentRequest (JSON)
Params: customerId (query)
Security: @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
Response: 200 OK + BookingResponseDto
```

**Documentation Swagger complète** sur les 3 endpoints.

---

## 📊 Statistiques

### Code créé
- **Lignes DTO:** ~40
- **Lignes interface:** ~70
- **Lignes tests:** ~240
- **Lignes service:** ~180
- **Lignes controller:** ~120
- **Total:** ~650 lignes

### Tests
- **Nouveaux:** 9
- **Total BookingService:** 22
- **Total projet:** 45 (13 Receiver + 10 Settings + 22 Booking)

### API
- **Nouveaux endpoints:** 3
- **Total endpoints:** 7

---

## 🔄 Transitions de Statuts

```
Flux complet d'une réservation:

1. Client crée réservation
   → PENDING_CONFIRMATION

2a. Voyageur CONFIRME
   → CONFIRMED_UNPAID
   + Deadline paiement définie

2b. Voyageur REJETTE
   → CANCELLED_BY_TRAVELER
   (Fin du flux)

3. Client PAIE (avant deadline)
   → CONFIRMED_PAID
   
4. [Sprint 4] Livraison
   → DELIVERED
```

---

## 💰 Calcul Deadline Paiement

**Configuration (PlatformSettings):**
```yaml
paymentTimeoutHours: 12  # Par défaut
```

**Exemple:**
```
Confirmation: 23 oct 2025, 16:00
Deadline:     24 oct 2025, 04:00  (16:00 + 12h)
```

**Validation:**
```java
if (LocalDateTime.now().isAfter(paymentDeadline)) {
    throw new SendByOpException(ErrorInfo.PAYMENT_FAILED,
            "Le délai de paiement est dépassé");
}
```

---

## 🧪 Scénarios de Tests

### Scénario 1: Flux Normal (Happy Path)
```
1. Client crée réservation → PENDING_CONFIRMATION ✓
2. Voyageur confirme → CONFIRMED_UNPAID ✓
3. Client paie → CONFIRMED_PAID ✓
```

### Scénario 2: Rejet par Voyageur
```
1. Client crée réservation → PENDING_CONFIRMATION ✓
2. Voyageur rejette → CANCELLED_BY_TRAVELER ✓
```

### Scénario 3: Double Confirmation (Erreur)
```
1. Client crée réservation → PENDING_CONFIRMATION ✓
2. Voyageur confirme → CONFIRMED_UNPAID ✓
3. Voyageur tente de confirmer à nouveau → ❌ INVALID_STATUS
```

### Scénario 4: Paiement Incorrect (Erreur)
```
1. Client crée réservation → PENDING_CONFIRMATION ✓
2. Voyageur confirme → CONFIRMED_UNPAID ✓
3. Client paie montant incorrect → ❌ INVALID_DATA
```

### Scénario 5: Non Autorisé (Erreur)
```
1. Client crée réservation → PENDING_CONFIRMATION ✓
2. Autre voyageur tente de confirmer → ❌ UNAUTHORIZED
```

---

## 🔐 Sécurité & Autorisations

### Confirmation/Rejet
```java
// Vérifier propriétaire du vol
Integer flightOwnerId = booking.getFlight().getCustomer().getId();
if (!flightOwnerId.equals(travelerId)) {
    throw new SendByOpException(ErrorInfo.UNAUTHORIZED, ...);
}
```

### Paiement
```java
// Vérifier propriétaire de la réservation
if (!booking.getCustomer().getId().equals(customerId)) {
    throw new SendByOpException(ErrorInfo.UNAUTHORIZED, ...);
}
```

**Spring Security:**
- `@PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")`
- Token JWT requis
- Validation rôle utilisateur

---

## 📈 Progression Globale

```
███████████████████░░░░░░░░░░░░░░░░░░░░░░░ 40%

✅ Sprint 1:  Infrastructure          100%
✅ Sprint 2a: ReceiverService         100%
✅ Sprint 2b: PlatformSettingsService 100%
✅ Sprint 2c: BookingService (create) 100%
✅ Sprint 3:  Confirmation/Paiement   100%
⏳ Sprint 4:  Annulation/Livraison      0%
⏳ Sprint 5:  Cron Jobs/Payout          0%
⏳ Sprint 6:  Notifications/Admin       0%
⏳ Sprint 7:  Tests/Documentation       0%
```

---

## 🚀 Prochaines Étapes: Sprint 4

### Annulation & Livraison

**Méthodes à implémenter:**
```java
// Client annule avant deadline
BookingResponseDto cancelByClient(Integer bookingId, Integer customerId, String reason);

// Annulation automatique (deadline dépassée)
void autoCancelUnpaidBookings();

// Marquer comme livré
BookingResponseDto markAsDelivered(Integer bookingId, Integer travelerId);

// Marquer comme récupéré
BookingResponseDto markAsPickedUp(Integer bookingId, Integer customerId);
```

**Transitions:**
```
CONFIRMED_UNPAID → CANCELLED_BY_CLIENT (annulation client)
CONFIRMED_UNPAID → CANCELLED_PAYMENT_TIMEOUT (auto, deadline)
CONFIRMED_PAID → IN_TRANSIT → DELIVERED → PICKED_UP
```

**Durée estimée:** 4-5 heures

---

## 📚 Utilisation API

### Exemple 1: Confirmation par Voyageur

```bash
PUT http://localhost:9002/api/bookings/1/confirm?travelerId=5
Authorization: Bearer {token}
```

**Réponse:**
```json
{
  "id": 1,
  "status": "CONFIRMED_UNPAID",
  "confirmedAt": "2025-10-23T16:30:00",
  "paymentDeadline": "2025-10-24T04:30:00",
  "totalPrice": 42.50,
  ...
}
```

### Exemple 2: Rejet par Voyageur

```bash
PUT http://localhost:9002/api/bookings/1/reject?travelerId=5&reason=Colis%20trop%20volumineux
Authorization: Bearer {token}
```

**Réponse:**
```json
{
  "id": 1,
  "status": "CANCELLED_BY_TRAVELER",
  ...
}
```

### Exemple 3: Paiement par Client

```bash
POST http://localhost:9002/api/bookings/1/payment?customerId=10
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 42.50,
  "paymentMethod": "CREDIT_CARD",
  "paymentToken": "tok_visa_test_123"
}
```

**Réponse:**
```json
{
  "id": 1,
  "status": "CONFIRMED_PAID",
  ...
}
```

---

## ⚠️ Points d'Attention

### 1. Deadline Paiement
- **Définie** lors de la confirmation
- **Calculée** selon `PlatformSettings.paymentTimeoutHours`
- **Vérifiée** avant traitement paiement
- **[Future]** Job cron pour annulation auto si dépassée

### 2. Validation Montant
- **Exact:** `paymentRequest.amount == booking.totalPrice`
- **Pas de tolérance** (même 1 centime d'écart = rejet)
- **Raison:** Éviter fraudes et erreurs

### 3. Gateway Paiement (Future)
- **Actuellement:** Mock (considéré réussi)
- **Phase future:** Intégration Stripe/PayPal
- **À faire:**
  ```java
  // PaymentGatewayService.charge(paymentRequest)
  // Gestion erreurs paiement
  // Webhook callbacks
  ```

### 4. Transactions
- `@Transactional` sur service
- Rollback auto si erreur
- Cohérence garantie

---

## 🎊 Résumé

**Sprint 3 complété avec succès !**

**Réalisations:**
- ✅ 3 méthodes service implémentées
- ✅ 9 tests unitaires (TDD)
- ✅ 3 endpoints API documentés
- ✅ 2 nouveaux codes d'erreur
- ✅ Gestion complète du cycle confirmation→paiement
- ✅ Validations robustes (autorisations, statuts, montants)

**Tests totaux:** 45 (13 + 10 + 22)  
**Endpoints totaux:** 7  
**Progression:** 40%

**Prochaine session:** Sprint 4 - Annulation & Livraison

---

**Excellente progression ! Le cœur du système de réservation est maintenant fonctionnel. 🎉**
