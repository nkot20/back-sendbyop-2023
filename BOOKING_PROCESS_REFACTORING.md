# Refonte du Processus de Réservation - SendByOp

**Date:** 23 octobre 2025  
**Approche:** Test-Driven Development (TDD)

---

## 🎯 Objectifs

### Problématiques Actuelles
- ❌ Mauvaise gestion des statuts (valeurs numériques)
- ❌ Enregistrement non cohérent du destinataire
- ❌ Pas de contrôle de doublons destinataire
- ❌ Manque de notifications automatisées
- ❌ Pas de gestion automatique des délais

### Solutions à Implémenter
- ✅ Énumérations pour les statuts
- ✅ Enregistrement automatique destinataire avec contrôle doublons
- ✅ Notifications email à chaque étape
- ✅ Cron jobs pour délais (paiement, versement)
- ✅ Paramétrage administrateur

---

## 📊 Modèle de Données

### Énumérations à Créer

#### 1. BookingStatus (Statut de Réservation)
```java
public enum BookingStatus {
    PENDING_CONFIRMATION,      // En attente confirmation voyageur
    CONFIRMED_UNPAID,          // Confirmée mais non payée
    CONFIRMED_PAID,            // Confirmée et payée
    IN_TRANSIT,                // En transit
    DELIVERED,                 // Livrée
    CONFIRMED_BY_RECEIVER,     // Réception confirmée
    CANCELLED_BY_CLIENT,       // Annulée par client
    CANCELLED_BY_TRAVELER,     // Rejetée par voyageur
    CANCELLED_PAYMENT_TIMEOUT, // Annulée (délai paiement dépassé)
    REFUNDED                   // Remboursée
}
```

#### 2. NotificationType (Type de Notification)
```java
public enum NotificationType {
    BOOKING_CREATED,           // Nouvelle réservation
    BOOKING_CONFIRMED,         // Réservation confirmée
    BOOKING_REJECTED,          // Réservation rejetée
    PAYMENT_RECEIVED,          // Paiement reçu
    PAYMENT_REMINDER,          // Rappel paiement
    DELIVERY_CONFIRMED,        // Livraison confirmée
    BOOKING_CANCELLED,         // Réservation annulée
    REFUND_PROCESSED           // Remboursement effectué
}
```

#### 3. RecipientStatus (Statut Destinataire)
```java
public enum RecipientStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED
}
```

### Entités à Modifier/Créer

#### 1. Booking (Réservation)
```java
@Entity
public class Booking {
    @Id
    @GeneratedValue
    private Integer id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    
    @ManyToOne
    private Customer customer;      // Client réserveur
    
    @ManyToOne
    private Receiver receiver;      // Destinataire
    
    @ManyToOne
    private Flight flight;          // Vol
    
    @OneToMany(mappedBy = "booking")
    private List<Parcel> parcels;   // Colis
    
    private String parcelPhotoUrl;  // Photo du colis
    
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    
    private BigDecimal totalPrice;
    private BigDecimal refundAmount;
    
    private String cancellationReason;
}
```

#### 2. Receiver (Destinataire) - À créer ou améliorer
```java
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = "phoneNumber"),
    @UniqueConstraint(columnNames = "email")
})
public class Receiver {
    @Id
    @GeneratedValue
    private Integer id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    
    private String address;
    private String city;
    private String country;
    
    @Enumerated(EnumType.STRING)
    private RecipientStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 3. PlatformSettings (Paramètres Plateforme)
```java
@Entity
public class PlatformSettings {
    @Id
    @GeneratedValue
    private Integer id;
    
    // Tarifs
    private BigDecimal minPricePerKg;
    private BigDecimal maxPricePerKg;
    
    // Répartition (en pourcentage)
    private BigDecimal travelerPercentage;   // Ex: 70%
    private BigDecimal platformPercentage;   // Ex: 25%
    private BigDecimal vatPercentage;        // Ex: 5%
    
    // Délais (en heures)
    private Integer paymentTimeoutHours;     // Default: 12h
    private Integer autoPayoutDelayHours;    // Default: 24h
    private Integer cancellationDeadlineHours; // Default: 24h
    
    // Pénalités
    private BigDecimal lateCancellationPenalty; // Ex: 50%
    
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

#### 4. NotificationLog (Log des Notifications)
```java
@Entity
public class NotificationLog {
    @Id
    @GeneratedValue
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @ManyToOne
    private Booking booking;
    
    private String recipientEmail;
    private String subject;
    private String content;
    
    private Boolean sent;
    private LocalDateTime sentAt;
    private String errorMessage;
}
```

---

## 🔄 Processus Détaillé

### Étape 1: Création de Réservation

**Acteur:** Client (Réserveur)

**Actions:**
1. Client sélectionne un vol
2. Client remplit les infos du colis
3. Client téléverse une photo du colis
4. Client renseigne les infos du destinataire

**Traitement Backend:**
```java
@Transactional
public BookingDto createBooking(CreateBookingRequest request) {
    // 1. Valider les données
    validateBookingRequest(request);
    
    // 2. Vérifier/créer le destinataire
    Receiver receiver = getOrCreateReceiver(request.getReceiverInfo());
    
    // 3. Uploader la photo du colis
    String photoUrl = uploadParcelPhoto(request.getParcelPhoto());
    
    // 4. Créer la réservation
    Booking booking = new Booking();
    booking.setStatus(BookingStatus.PENDING_CONFIRMATION);
    booking.setCustomer(currentCustomer);
    booking.setReceiver(receiver);
    booking.setFlight(flight);
    booking.setParcelPhotoUrl(photoUrl);
    booking.setCreatedAt(LocalDateTime.now());
    
    Booking saved = bookingRepository.save(booking);
    
    // 5. Envoyer notifications
    sendBookingCreatedNotifications(saved);
    
    return mapper.toDto(saved);
}
```

**Notifications:**
- **Voyageur:** "Nouvelle réservation pour votre vol [destination]"
- **Client:** "Votre réservation a été créée avec succès"
- **Destinataire:** "Un colis vous sera livré prochainement"

**Tests à Créer:**
```java
@Test
void shouldCreateBookingWithPendingStatus()
@Test
void shouldCreateReceiverIfNotExists()
@Test
void shouldUseExistingReceiverIfEmailExists()
@Test
void shouldThrowExceptionIfParcelPhotoMissing()
@Test
void shouldSendNotificationsToAllParties()
```

---

### Étape 2: Confirmation par Voyageur

**Acteur:** Voyageur

**Actions:**
1. Voyageur consulte la photo du colis
2. Voyageur accepte ou rejette la réservation

**Traitement Backend:**

#### Cas 1: Confirmation
```java
@Transactional
public BookingDto confirmBooking(Integer bookingId) {
    Booking booking = getBooking(bookingId);
    validateTravelerOwnsBooking(booking);
    
    // Vérifier statut
    if (booking.getStatus() != BookingStatus.PENDING_CONFIRMATION) {
        throw new InvalidStatusException();
    }
    
    // Mettre à jour
    booking.setStatus(BookingStatus.CONFIRMED_UNPAID);
    booking.setConfirmedAt(LocalDateTime.now());
    
    // Calculer deadline paiement
    PlatformSettings settings = getSettings();
    LocalDateTime paymentDeadline = LocalDateTime.now()
        .plusHours(settings.getPaymentTimeoutHours());
    booking.setPaymentDeadline(paymentDeadline);
    
    Booking saved = bookingRepository.save(booking);
    
    // Notification
    sendBookingConfirmedNotification(saved);
    
    return mapper.toDto(saved);
}
```

**Notification Client:**
```
Sujet: Votre réservation a été confirmée
Corps: Le voyageur a accepté votre réservation.
       Vous devez effectuer le paiement avant le [deadline].
       Passé ce délai, la réservation sera annulée automatiquement.
```

#### Cas 2: Rejet
```java
@Transactional
public BookingDto rejectBooking(Integer bookingId, String reason) {
    Booking booking = getBooking(bookingId);
    booking.setStatus(BookingStatus.CANCELLED_BY_TRAVELER);
    booking.setCancelledAt(LocalDateTime.now());
    booking.setCancellationReason(reason);
    
    Booking saved = bookingRepository.save(booking);
    sendBookingRejectedNotification(saved);
    
    return mapper.toDto(saved);
}
```

**Tests:**
```java
@Test
void shouldConfirmBookingAndSetDeadline()
@Test
void shouldRejectBookingWithReason()
@Test
void shouldThrowExceptionIfNotPendingStatus()
@Test
void shouldSendPaymentReminderEmail()
```

---

### Étape 3: Paiement

**Acteur:** Client

**Actions:**
1. Client procède au paiement via le système

**Traitement Backend:**
```java
@Transactional
public PaymentDto processPayment(Integer bookingId, PaymentRequest request) {
    Booking booking = getBooking(bookingId);
    
    // Vérifier statut et deadline
    if (booking.getStatus() != BookingStatus.CONFIRMED_UNPAID) {
        throw new InvalidStatusException();
    }
    
    if (LocalDateTime.now().isAfter(booking.getPaymentDeadline())) {
        throw new PaymentDeadlineExpiredException();
    }
    
    // Traiter le paiement
    Payment payment = paymentService.processPayment(request);
    
    // Mettre à jour booking
    booking.setStatus(BookingStatus.CONFIRMED_PAID);
    booking.setPaidAt(LocalDateTime.now());
    booking.setPayment(payment);
    
    Booking saved = bookingRepository.save(booking);
    
    // Notifications
    sendPaymentConfirmedNotifications(saved);
    
    return paymentMapper.toDto(payment);
}
```

**Notifications:**
- **Client:** "Paiement confirmé"
- **Voyageur:** "Le client a payé pour la réservation"
- **Destinataire:** "Votre colis est confirmé et sera bientôt en transit"

**Tests:**
```java
@Test
void shouldProcessPaymentAndUpdateStatus()
@Test
void shouldThrowExceptionIfDeadlinePassed()
@Test
void shouldSendNotificationsToAllParties()
```

---

### Étape 4: Cron Job - Annulation Automatique

**Déclencheur:** Cron (toutes les heures)

**Action:** Annuler les réservations confirmées non payées après 12h

```java
@Scheduled(cron = "0 0 * * * *") // Toutes les heures
@Transactional
public void cancelUnpaidBookings() {
    PlatformSettings settings = getSettings();
    LocalDateTime deadline = LocalDateTime.now()
        .minusHours(settings.getPaymentTimeoutHours());
    
    List<Booking> unpaidBookings = bookingRepository
        .findByStatusAndConfirmedAtBefore(
            BookingStatus.CONFIRMED_UNPAID, 
            deadline
        );
    
    for (Booking booking : unpaidBookings) {
        booking.setStatus(BookingStatus.CANCELLED_PAYMENT_TIMEOUT);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason("Délai de paiement dépassé");
        
        bookingRepository.save(booking);
        sendTimeoutCancellationNotification(booking);
    }
    
    log.info("Cancelled {} unpaid bookings", unpaidBookings.size());
}
```

**Tests:**
```java
@Test
void shouldCancelBookingsAfter12Hours()
@Test
void shouldNotCancelBookingsBefore12Hours()
@Test
void shouldSendCancellationNotification()
```

---

### Étape 5: Annulation par Client

**Acteur:** Client

**Règles:**
- Si > 24h avant vol: Remboursement intégral
- Si < 24h avant vol: Retenue 50%
- Si non payé: Aucun remboursement

```java
@Transactional
public RefundDto cancelBooking(Integer bookingId, String reason) {
    Booking booking = getBooking(bookingId);
    validateCustomerOwnsBooking(booking);
    
    // Calculer délai
    PlatformSettings settings = getSettings();
    LocalDateTime cancellationDeadline = booking.getFlight()
        .getDepartureDate()
        .minusHours(settings.getCancellationDeadlineHours());
    
    boolean isLateCancellation = LocalDateTime.now()
        .isAfter(cancellationDeadline);
    
    // Calculer remboursement
    BigDecimal refundAmount = BigDecimal.ZERO;
    
    if (booking.getStatus() == BookingStatus.CONFIRMED_PAID) {
        if (isLateCancellation) {
            // 50% de retenue
            refundAmount = booking.getTotalPrice()
                .multiply(BigDecimal.ONE
                    .subtract(settings.getLateCancellationPenalty()));
        } else {
            // Remboursement intégral
            refundAmount = booking.getTotalPrice();
        }
    }
    
    // Mettre à jour booking
    booking.setStatus(BookingStatus.CANCELLED_BY_CLIENT);
    booking.setCancelledAt(LocalDateTime.now());
    booking.setCancellationReason(reason);
    booking.setRefundAmount(refundAmount);
    
    Booking saved = bookingRepository.save(booking);
    
    // Traiter remboursement si applicable
    Refund refund = null;
    if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
        refund = refundService.processRefund(saved, refundAmount);
    }
    
    // Notifications
    sendCancellationNotifications(saved);
    
    return refundMapper.toDto(refund);
}
```

**Tests:**
```java
@Test
void shouldRefundFullAmountIfMoreThan24Hours()
@Test
void shouldRefund50PercentIfLessThan24Hours()
@Test
void shouldNotRefundIfNotPaid()
@Test
void shouldSendCancellationNotifications()
```

---

### Étape 6: Livraison et Confirmation

**Acteur:** Destinataire

```java
@Transactional
public BookingDto confirmDelivery(Integer bookingId) {
    Booking booking = getBooking(bookingId);
    
    if (booking.getStatus() != BookingStatus.DELIVERED) {
        throw new InvalidStatusException();
    }
    
    booking.setStatus(BookingStatus.CONFIRMED_BY_RECEIVER);
    booking.setConfirmedAt(LocalDateTime.now());
    
    Booking saved = bookingRepository.save(booking);
    
    // Déclencher versement immédiat
    payoutService.processPayout(saved);
    
    // Notifications
    sendDeliveryConfirmedNotifications(saved);
    
    return mapper.toDto(saved);
}
```

---

### Étape 7: Cron Job - Versement Automatique

**Déclencheur:** Cron (toutes les heures)

**Action:** Verser les montants après 24h sans confirmation

```java
@Scheduled(cron = "0 0 * * * *")
@Transactional
public void processAutomaticPayouts() {
    PlatformSettings settings = getSettings();
    LocalDateTime deadline = LocalDateTime.now()
        .minusHours(settings.getAutoPayoutDelayHours());
    
    List<Booking> deliveredBookings = bookingRepository
        .findByStatusAndDeliveredAtBefore(
            BookingStatus.DELIVERED, 
            deadline
        );
    
    for (Booking booking : deliveredBookings) {
        try {
            payoutService.processPayout(booking);
            
            booking.setStatus(BookingStatus.CONFIRMED_BY_RECEIVER);
            booking.setConfirmedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            
        } catch (Exception e) {
            log.error("Failed to process payout for booking {}", 
                booking.getId(), e);
        }
    }
    
    log.info("Processed {} automatic payouts", deliveredBookings.size());
}
```

**Tests:**
```java
@Test
void shouldProcessPayoutsAfter24Hours()
@Test
void shouldNotProcessPayoutsBefore24Hours()
@Test
void shouldHandlePayoutFailures()
```

---

## 💰 Service de Versement (Payout)

```java
@Service
public class PayoutService {
    
    @Transactional
    public Payout processPayout(Booking booking) {
        PlatformSettings settings = getSettings();
        
        // Calculer répartition
        BigDecimal totalAmount = booking.getTotalPrice();
        
        BigDecimal travelerAmount = totalAmount
            .multiply(settings.getTravelerPercentage())
            .divide(BigDecimal.valueOf(100));
            
        BigDecimal platformAmount = totalAmount
            .multiply(settings.getPlatformPercentage())
            .divide(BigDecimal.valueOf(100));
            
        BigDecimal vatAmount = totalAmount
            .multiply(settings.getVatPercentage())
            .divide(BigDecimal.valueOf(100));
        
        // Créer payout
        Payout payout = new Payout();
        payout.setBooking(booking);
        payout.setTraveler(booking.getFlight().getCustomer());
        payout.setTotalAmount(totalAmount);
        payout.setTravelerAmount(travelerAmount);
        payout.setPlatformAmount(platformAmount);
        payout.setVatAmount(vatAmount);
        payout.setStatus(PayoutStatus.PENDING);
        payout.setCreatedAt(LocalDateTime.now());
        
        // Traiter le versement (intégration paiement)
        paymentGateway.transferToTraveler(
            booking.getFlight().getCustomer(),
            travelerAmount
        );
        
        payout.setStatus(PayoutStatus.COMPLETED);
        payout.setCompletedAt(LocalDateTime.now());
        
        return payoutRepository.save(payout);
    }
}
```

---

## 📧 Service de Notifications

```java
@Service
public class BookingNotificationService {
    
    public void sendBookingCreatedNotifications(Booking booking) {
        // Notification voyageur
        sendEmail(
            booking.getFlight().getCustomer().getEmail(),
            "Nouvelle réservation pour votre vol",
            emailTemplateService.generateBookingCreated(booking, "TRAVELER")
        );
        
        // Notification client
        sendEmail(
            booking.getCustomer().getEmail(),
            "Réservation créée avec succès",
            emailTemplateService.generateBookingCreated(booking, "CUSTOMER")
        );
        
        // Notification destinataire
        sendEmail(
            booking.getReceiver().getEmail(),
            "Un colis vous sera livré",
            emailTemplateService.generateBookingCreated(booking, "RECEIVER")
        );
        
        // Log
        logNotification(booking, NotificationType.BOOKING_CREATED);
    }
    
    // Méthodes similaires pour chaque type de notification
}
```

---

## ⚙️ Paramétrage Administrateur

### Controller
```java
@RestController
@RequestMapping("/api/admin/settings")
public class PlatformSettingsController {
    
    @GetMapping
    public PlatformSettingsDto getSettings() {
        return settingsService.getSettings();
    }
    
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PlatformSettingsDto updateSettings(
        @Valid @RequestBody PlatformSettingsDto dto
    ) {
        return settingsService.updateSettings(dto);
    }
}
```

### Validation
```java
@Data
public class PlatformSettingsDto {
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal minPricePerKg;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal maxPricePerKg;
    
    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal travelerPercentage;
    
    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal platformPercentage;
    
    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal vatPercentage;
    
    @NotNull
    @Min(2)
    @Max(24)
    private Integer paymentTimeoutHours;
    
    @NotNull
    @Min(12)
    @Max(72)
    private Integer autoPayoutDelayHours;
    
    // Validation custom: total = 100%
    @AssertTrue(message = "La somme des pourcentages doit être égale à 100%")
    private boolean isPercentageSumValid() {
        return travelerPercentage
            .add(platformPercentage)
            .add(vatPercentage)
            .compareTo(BigDecimal.valueOf(100)) == 0;
    }
}
```

---

## 📋 Checklist d'Implémentation (TDD)

### Phase 1: Énumérations et Entités
- [ ] Créer BookingStatus enum
- [ ] Créer NotificationType enum
- [ ] Créer RecipientStatus enum
- [ ] Créer/Modifier entité Receiver
- [ ] Modifier entité Booking
- [ ] Créer entité PlatformSettings
- [ ] Créer entité NotificationLog
- [ ] Créer entité Payout

### Phase 2: Receiver Service (TDD)
- [ ] Tests: createReceiver, getByEmail, getByPhone
- [ ] Tests: checkDuplicates
- [ ] Implémentation ReceiverService
- [ ] Implémentation ReceiverRepository

### Phase 3: Booking Creation (TDD)
- [ ] Tests: createBooking avec tous les cas
- [ ] Tests: uploadParcelPhoto
- [ ] Tests: getOrCreateReceiver
- [ ] Implémentation BookingService.createBooking
- [ ] Implémentation notifications

### Phase 4: Confirmation/Rejet (TDD)
- [ ] Tests: confirmBooking
- [ ] Tests: rejectBooking
- [ ] Tests: deadline calculation
- [ ] Implémentation

### Phase 5: Paiement (TDD)
- [ ] Tests: processPayment
- [ ] Tests: deadline validation
- [ ] Tests: notifications
- [ ] Implémentation

### Phase 6: Annulation (TDD)
- [ ] Tests: cancelBooking
- [ ] Tests: refund calculation (full/partial/none)
- [ ] Tests: deadline check
- [ ] Implémentation

### Phase 7: Livraison (TDD)
- [ ] Tests: confirmDelivery
- [ ] Tests: trigger payout
- [ ] Implémentation

### Phase 8: Cron Jobs (TDD)
- [ ] Tests: cancelUnpaidBookings
- [ ] Tests: processAutomaticPayouts
- [ ] Implémentation avec @Scheduled
- [ ] Configuration cron

### Phase 9: Payout Service (TDD)
- [ ] Tests: processPayout
- [ ] Tests: calculate splits
- [ ] Tests: payment gateway integration
- [ ] Implémentation

### Phase 10: Notifications (TDD)
- [ ] Tests: chaque type de notification
- [ ] Templates email Thymeleaf
- [ ] Implémentation service
- [ ] Logging notifications

### Phase 11: Admin Settings (TDD)
- [ ] Tests: getSettings, updateSettings
- [ ] Tests: validation pourcentages
- [ ] Implémentation controller
- [ ] Implémentation service

---

## 🚀 Ordre d'Exécution

1. **Sprint 1 (Semaine 1):** Énumérations + Entités + Migrations
2. **Sprint 2 (Semaine 2):** Receiver Service + Booking Creation
3. **Sprint 3 (Semaine 3):** Confirmation + Paiement
4. **Sprint 4 (Semaine 4):** Annulation + Livraison
5. **Sprint 5 (Semaine 5):** Cron Jobs + Payout
6. **Sprint 6 (Semaine 6):** Notifications + Admin Settings
7. **Sprint 7 (Semaine 7):** Tests d'intégration + Documentation

---

**Prêt à commencer par la Phase 1 ?**
