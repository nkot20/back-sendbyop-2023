# Sprint 2: Services & Logique Métier - En cours

**Date début:** 23 octobre 2025  
**Statut:** 🚧 EN COURS  
**Approche:** Test-Driven Development (TDD)

---

## ✅ Travail Accompli

### 1. Repository ReceiverRepository
**Fichier:** `ReceiverRepository.java`

**Méthodes ajoutées:**
- `findByEmail(String email)` - Recherche par email
- `findByPhoneNumber(String phoneNumber)` - Recherche par téléphone
- `existsByEmail(String email)` - Vérification doublon email
- `existsByPhoneNumber(String phoneNumber)` - Vérification doublon téléphone

### 2. DTO ReceiverDto
**Fichier:** `ReceiverDto.java`

**Champs mis à jour:**
- `phoneNumber` (remplace `phone`)
- Ajout `address`, `city`, `country`
- Ajout `status` (RecipientStatus)
- Ajout `createdAt`, `updatedAt`
- Validations Jakarta (`@NotBlank`, `@Email`)

### 3. Mapper ReceiverMapper
**Fichier:** `ReceiverMapper.java` ✅ Existant et fonctionnel

---

## 🎯 Prochaines Étapes - Sprint 2

### Phase 2.1: ReceiverService (TDD)

#### Étape 1: Créer les Tests (RED)
**Fichier à créer:** `ReceiverServiceTest.java`

**Tests à implémenter:**

```java
@SpringBootTest
@Transactional
class ReceiverServiceTest {
    
    // TEST 1: Création destinataire avec données valides
    @Test
    void shouldCreateReceiverWithValidData()
    
    // TEST 2: Récupération destinataire existant par email
    @Test
    void shouldGetExistingReceiverByEmail()
    
    // TEST 3: Récupération destinataire existant par téléphone
    @Test
    void shouldGetExistingReceiverByPhoneNumber()
    
    // TEST 4: Créer ou récupérer - cas création
    @Test
    void shouldCreateReceiverWhenNotExists()
    
    // TEST 5: Créer ou récupérer - cas récupération par email
    @Test
    void shouldRetrieveReceiverWhenEmailExists()
    
    // TEST 6: Créer ou récupérer - cas récupération par téléphone
    @Test
    void shouldRetrieveReceiverWhenPhoneExists()
    
    // TEST 7: Échec si email et téléphone null
    @Test
    void shouldThrowExceptionWhenBothEmailAndPhoneAreNull()
    
    // TEST 8: Échec si email invalide
    @Test
    void shouldThrowExceptionWhenEmailInvalid()
    
    // TEST 9: Mise à jour destinataire existant
    @Test
    void shouldUpdateExistingReceiver()
    
    // TEST 10: Vérifier contrôle doublon email
    @Test
    void shouldDetectDuplicateEmail()
    
    // TEST 11: Vérifier contrôle doublon téléphone
    @Test
    void shouldDetectDuplicatePhoneNumber()
}
```

#### Étape 2: Créer l'Interface IReceiverService
**Fichier à créer:** `IReceiverService.java`

```java
public interface IReceiverService {
    
    /**
     * Crée un nouveau destinataire
     */
    ReceiverDto createReceiver(ReceiverDto receiverDto) 
        throws SendByOpException;
    
    /**
     * Récupère un destinataire par email
     */
    ReceiverDto getReceiverByEmail(String email) 
        throws SendByOpException;
    
    /**
     * Récupère un destinataire par numéro de téléphone
     */
    ReceiverDto getReceiverByPhoneNumber(String phoneNumber) 
        throws SendByOpException;
    
    /**
     * Récupère un destinataire existant ou le crée
     * Recherche par email OU téléphone
     */
    ReceiverDto getOrCreateReceiver(ReceiverDto receiverDto) 
        throws SendByOpException;
    
    /**
     * Met à jour un destinataire existant
     */
    ReceiverDto updateReceiver(ReceiverDto receiverDto) 
        throws SendByOpException;
    
    /**
     * Vérifie si un destinataire existe (email OU téléphone)
     */
    boolean receiverExists(String email, String phoneNumber);
}
```

#### Étape 3: Implémenter ReceiverService (GREEN)
**Fichier à créer:** `ReceiverService.java`

**Points clés:**
- Contrôle de doublons sur email ET téléphone
- Méthode `getOrCreateReceiver()` intelligente:
  1. Chercher par email si fourni
  2. Sinon chercher par téléphone si fourni
  3. Sinon créer nouveau destinataire
- Validation des données avec Jakarta Validation
- Logging approprié
- Gestion des exceptions

**Exemple de logique `getOrCreateReceiver()`:**

```java
public ReceiverDto getOrCreateReceiver(ReceiverDto receiverDto) 
    throws SendByOpException {
    
    log.debug("Getting or creating receiver: {}", receiverDto.getEmail());
    
    // Validation
    if (StringUtils.isEmpty(receiverDto.getEmail()) && 
        StringUtils.isEmpty(receiverDto.getPhoneNumber())) {
        throw new SendByOpException(ErrorInfo.INVALID_DATA, 
            "Email ou téléphone requis");
    }
    
    // Chercher par email
    if (StringUtils.isNotEmpty(receiverDto.getEmail())) {
        Optional<Receiver> existing = 
            receiverRepository.findByEmail(receiverDto.getEmail());
        if (existing.isPresent()) {
            log.debug("Receiver found by email");
            return receiverMapper.toDto(existing.get());
        }
    }
    
    // Chercher par téléphone
    if (StringUtils.isNotEmpty(receiverDto.getPhoneNumber())) {
        Optional<Receiver> existing = 
            receiverRepository.findByPhoneNumber(receiverDto.getPhoneNumber());
        if (existing.isPresent()) {
            log.debug("Receiver found by phone");
            return receiverMapper.toDto(existing.get());
        }
    }
    
    // Créer nouveau
    log.debug("Creating new receiver");
    return createReceiver(receiverDto);
}
```

#### Étape 4: Refactoring (REFACTOR)
- Optimiser les requêtes
- Améliorer les logs
- Ajouter des métriques si besoin
- Vérifier couverture de tests

---

### Phase 2.2: PlatformSettingsService (TDD)

#### Tests à créer:
```java
@Test
void shouldGetDefaultSettings()

@Test
void shouldUpdateSettings()

@Test
void shouldValidatePercentageSum()

@Test
void shouldValidatePriceRange()

@Test
void shouldRejectInvalidTimeout()
```

#### Service à implémenter:
- CRUD simple sur PlatformSettings
- Validation automatique des contraintes
- Une seule ligne en base (singleton)
- Cache sur le GET

---

### Phase 2.3: Préparation BookingService (Partiel)

#### Composants nécessaires:
1. **CreateBookingRequest** (DTO)
   - Infos vol
   - Infos destinataire
   - Photo colis (MultipartFile)
   - Infos colis

2. **BookingService.createBooking()** (Méthode de base)
   - Validation vol existe
   - Création/récupération destinataire
   - Upload photo colis
   - Sauvegarde booking
   - Envoi notifications

---

## 📊 Statistiques Sprint 2

### Complété
- [x] ReceiverRepository (4 méthodes)
- [x] ReceiverDto (enrichi avec validations)
- [x] ReceiverMapper (vérifié)

### En Cours
- [ ] ReceiverService Tests (0/11)
- [ ] IReceiverService Interface
- [ ] ReceiverService Implementation
- [ ] ReceiverService Refactoring

### À Faire
- [ ] PlatformSettingsRepository
- [ ] PlatformSettingsService Tests
- [ ] IPlatformSettingsService Interface
- [ ] PlatformSettingsService Implementation
- [ ] CreateBookingRequest DTO
- [ ] BookingService (création réservation de base)

---

## 🔧 Commandes Utiles

### Exécuter les Tests
```bash
# Tous les tests
.\mvnw.cmd test

# Tests ReceiverService uniquement
.\mvnw.cmd test -Dtest=ReceiverServiceTest

# Avec couverture
.\mvnw.cmd test jacoco:report
```

### Nettoyer Cache IDE
```bash
Remove-Item -Recurse -Force target
.\mvnw.cmd clean compile
```

---

## 📝 Notes Importantes

### Gestion des Doublons
**Règle:** Un destinataire est unique par **email OU téléphone**

**Scénarios:**
- Email existe → Retourner existant
- Téléphone existe → Retourner existant
- Aucun des deux n'existe → Créer nouveau
- Les deux existent mais correspondent à 2 destinataires différents → **ERREUR**

### Validation
- Email: Format valide (@)
- Téléphone: Non vide
- Nom/Prénom: Non vides
- Adresse: Optionnelle mais recommandée

### Status par Défaut
Nouveau destinataire = `RecipientStatus.ACTIVE`

---

## 🎯 Objectif Sprint 2

**Livrer:**
1. ✅ ReceiverService complet avec tests (TDD)
2. ✅ PlatformSettingsService complet avec tests (TDD)
3. ✅ Bases de BookingService (création réservation)

**Critères de succès:**
- Tous les tests passent
- Couverture > 80%
- Contrôle doublons fonctionnel
- Documentation complète

---

**Prochaine action:** Créer `ReceiverServiceTest.java` avec les 11 tests (phase RED) 🔴
