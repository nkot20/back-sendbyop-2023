# Guide de Développement TDD - SendByOp

**Date:** 23 octobre 2025  
**Approche:** Test-Driven Development (TDD)

---

## 🎯 Principe de Base

**Pour chaque nouvelle fonctionnalité, nous suivons le cycle TDD :**

```
1. RED   → Écrire un test qui échoue
2. GREEN → Écrire le code minimal pour que le test passe
3. REFACTOR → Améliorer le code tout en gardant les tests verts
```

---

## 📋 Workflow TDD Standard

### Étape 1: Définir la Fonctionnalité

**Avant d'écrire du code :**
- Définir clairement le besoin
- Identifier les cas d'usage
- Lister les scénarios de test (nominal, erreurs, edge cases)

### Étape 2: Écrire les Tests (RED)

**Créer les tests AVANT le code :**

```java
@Test
void shouldCreateFlightWhenValidData() {
    // Given
    FlightDto flightDto = createValidFlightDto();
    
    // When
    FlightDto result = flightService.saveVol(flightDto);
    
    // Then
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(flightDto.getDepartureAirportId(), result.getDepartureAirportId());
}

@Test
void shouldThrowExceptionWhenInvalidAirport() {
    // Given
    FlightDto flightDto = createFlightDtoWithInvalidAirport();
    
    // When & Then
    assertThrows(SendByOpException.class, () -> {
        flightService.saveVol(flightDto);
    });
}
```

**Exécuter les tests → Ils doivent ÉCHOUER (RED)**

### Étape 3: Implémenter le Code (GREEN)

**Écrire le code minimal pour faire passer les tests :**

```java
@Service
public class FlightService {
    public FlightDto saveVol(FlightDto flightDto) {
        // Implémentation minimale
        validateAirports(flightDto);
        Flight flight = flightMapper.toEntity(flightDto);
        Flight saved = flightRepository.save(flight);
        return flightMapper.toDto(saved);
    }
}
```

**Exécuter les tests → Ils doivent PASSER (GREEN)**

### Étape 4: Refactoriser (REFACTOR)

**Améliorer le code tout en gardant les tests verts :**
- Extraire des méthodes
- Améliorer la lisibilité
- Optimiser les performances
- Réduire la duplication

**Exécuter les tests après chaque refactoring → Toujours VERTS**

---

## 🏗️ Structure des Tests

### Organisation des Fichiers

```
src/test/java/com/sendByOP/expedition/
├── services/impl/
│   ├── FlightServiceTest.java
│   ├── BookingServiceTest.java
│   └── CustomerServiceTest.java
├── controllers/
│   ├── FlightControllerTest.java
│   └── BookingControllerTest.java
└── integration/
    ├── FlightIntegrationTest.java
    └── BookingIntegrationTest.java
```

### Conventions de Nommage

```java
// Pattern: should[ExpectedBehavior]When[Condition]
@Test
void shouldReturnFlightWhenIdExists() { ... }

@Test
void shouldThrowExceptionWhenIdNotFound() { ... }

@Test
void shouldUpdateFlightWhenValidData() { ... }
```

---

## 🧪 Types de Tests

### 1. Tests Unitaires

**Objectif:** Tester une méthode/classe isolément

```java
@ExtendWith(MockitoExtension.class)
class FlightServiceTest {
    
    @Mock
    private FlightRepository flightRepository;
    
    @Mock
    private FlightMapper flightMapper;
    
    @InjectMocks
    private FlightService flightService;
    
    @Test
    void shouldSaveFlightWhenValidData() {
        // Given
        FlightDto dto = createValidFlightDto();
        Flight entity = createFlightEntity();
        
        when(flightMapper.toEntity(dto)).thenReturn(entity);
        when(flightRepository.save(entity)).thenReturn(entity);
        when(flightMapper.toDto(entity)).thenReturn(dto);
        
        // When
        FlightDto result = flightService.saveVol(dto);
        
        // Then
        assertNotNull(result);
        verify(flightRepository).save(entity);
    }
}
```

### 2. Tests d'Intégration

**Objectif:** Tester l'interaction entre plusieurs composants

```java
@SpringBootTest
@Transactional
class FlightIntegrationTest {
    
    @Autowired
    private FlightService flightService;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Test
    void shouldSaveFlightAndPersistToDatabase() {
        // Given
        FlightDto flightDto = createValidFlightDto();
        
        // When
        FlightDto saved = flightService.saveVol(flightDto);
        
        // Then
        assertNotNull(saved.getId());
        
        Flight fromDb = flightRepository.findById(saved.getId()).orElseThrow();
        assertEquals(saved.getDepartureAirportId(), fromDb.getDepartureAirport().getId());
    }
}
```

### 3. Tests de Controller (API)

**Objectif:** Tester les endpoints REST

```java
@WebMvcTest(FlightController.class)
class FlightControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FlightService flightService;
    
    @Test
    void shouldReturnFlightListWhenGetAllFlights() throws Exception {
        // Given
        List<FlightDto> flights = List.of(createFlightDto());
        when(flightService.getAllVol()).thenReturn(flights);
        
        // When & Then
        mockMvc.perform(get("/flights")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(flights.get(0).getId()));
    }
}
```

---

## 📊 Couverture de Tests

### Objectifs Minimaux

- **Services:** 80% de couverture
- **Controllers:** 70% de couverture
- **Repositories:** Tests d'intégration pour requêtes custom
- **Mappers:** Tests unitaires pour mappings complexes

### Commandes Maven

```bash
# Exécuter tous les tests
.\mvnw.cmd test

# Exécuter avec couverture
.\mvnw.cmd test jacoco:report

# Voir le rapport
target/site/jacoco/index.html
```

---

## ✅ Checklist TDD pour Nouvelle Fonctionnalité

### Avant de Coder

- [ ] Fonctionnalité clairement définie
- [ ] Cas d'usage identifiés
- [ ] Scénarios de test listés (nominal + erreurs)

### Phase RED (Tests)

- [ ] Tests unitaires écrits
- [ ] Tests d'intégration écrits (si nécessaire)
- [ ] Tests de controller écrits
- [ ] Tous les tests ÉCHOUENT (comportement attendu)

### Phase GREEN (Implémentation)

- [ ] Code minimal implémenté
- [ ] Tous les tests PASSENT
- [ ] Pas de code mort ou inutile

### Phase REFACTOR (Amélioration)

- [ ] Code refactorisé (lisibilité, performance)
- [ ] Duplication éliminée
- [ ] Tous les tests toujours VERTS
- [ ] Code review effectué

### Documentation

- [ ] Javadoc ajoutée
- [ ] README mis à jour si nécessaire
- [ ] Tests documentés (scénarios complexes)

---

## 🎨 Patterns de Test

### Given-When-Then (Arrange-Act-Assert)

```java
@Test
void shouldCalculateTotalPriceWhenBookingCreated() {
    // Given (Arrange) - Préparer les données
    BookingDto booking = createBooking();
    booking.setParcelWeight(5.0);
    booking.setPricePerKg(10.0);
    
    // When (Act) - Exécuter l'action
    BookingDto result = bookingService.createBooking(booking);
    
    // Then (Assert) - Vérifier le résultat
    assertEquals(50.0, result.getTotalPrice());
    assertNotNull(result.getId());
}
```

### Test Data Builders

```java
public class FlightDtoBuilder {
    private Integer id;
    private Integer departureAirportId = 1;
    private Integer arrivalAirportId = 2;
    private Date departureDate = new Date();
    
    public static FlightDtoBuilder aFlight() {
        return new FlightDtoBuilder();
    }
    
    public FlightDtoBuilder withId(Integer id) {
        this.id = id;
        return this;
    }
    
    public FlightDtoBuilder withDepartureAirport(Integer airportId) {
        this.departureAirportId = airportId;
        return this;
    }
    
    public FlightDto build() {
        FlightDto dto = new FlightDto();
        dto.setId(id);
        dto.setDepartureAirportId(departureAirportId);
        dto.setArrivalAirportId(arrivalAirportId);
        dto.setDepartureDate(departureDate);
        return dto;
    }
}

// Usage:
FlightDto flight = aFlight()
    .withId(1)
    .withDepartureAirport(10)
    .build();
```

---

## 🚫 Anti-Patterns à Éviter

### ❌ Écrire le code avant les tests
```java
// MAL - Code avant tests
public FlightDto saveVol(FlightDto dto) {
    // Implémentation complète
}

// Tests écrits après coup
@Test void testSaveVol() { ... }
```

### ❌ Tests qui testent tout
```java
// MAL - Test trop large
@Test
void testEverything() {
    // Teste création, validation, persistance, mapping...
}
```

### ❌ Tests dépendants
```java
// MAL - Tests qui dépendent de l'ordre d'exécution
@Test void test1_createFlight() { ... }
@Test void test2_updateFlight() { ... } // Dépend de test1
```

### ❌ Assertions vagues
```java
// MAL
assertTrue(result != null);

// BIEN
assertNotNull(result);
assertEquals(expectedValue, result.getValue());
```

---

## 📚 Exemples Concrets SendByOp

### Exemple 1: Création de Vol

**1. Tests (RED)**
```java
@Test
void shouldCreateFlightWhenCustomerIsAuthenticated() {
    // Given
    FlightDto flightDto = aFlight()
        .withDepartureAirport(1)
        .withArrivalAirport(2)
        .build();
    
    when(authentication.getName()).thenReturn("customer@example.com");
    when(customerService.getCustomerByEmail("customer@example.com"))
        .thenReturn(createCustomerDto());
    
    // When
    FlightDto result = flightService.saveVolWithEscales(flightDto);
    
    // Then
    assertNotNull(result.getId());
    assertEquals(1, result.getDepartureAirportId());
}

@Test
void shouldThrowExceptionWhenCustomerNotFound() {
    // Given
    when(authentication.getName()).thenReturn("unknown@example.com");
    when(customerService.getCustomerByEmail(any()))
        .thenThrow(new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
    
    // When & Then
    assertThrows(SendByOpException.class, () -> {
        flightService.saveVolWithEscales(createFlightDto());
    });
}
```

**2. Implémentation (GREEN)**
```java
@Transactional
public FlightDto saveVolWithEscales(VolEscaleDto dto) {
    String username = SecurityContextHolder.getContext()
        .getAuthentication().getName();
    
    CustomerDto customer = customerService.getCustomerByEmail(username);
    
    Flight flight = flightMapper.toEntity(dto.getVol());
    flight.setCustomer(customerMapper.toEntity(customer));
    
    Flight saved = flightRepository.save(flight);
    return flightMapper.toDto(saved);
}
```

**3. Refactor**
- Extraire la logique d'authentification
- Ajouter des logs
- Optimiser les requêtes

### Exemple 2: Validation de Statut de Compte

**1. Tests (RED)**
```java
@Test
void shouldAllowLoginWhenAccountIsActive() {
    // Given
    User user = createUser();
    user.setStatus(AccountStatus.ACTIVE);
    
    // When & Then
    assertDoesNotThrow(() -> authService.validateAccountStatus(user));
}

@Test
void shouldThrowExceptionWhenAccountIsPendingVerification() {
    // Given
    User user = createUser();
    user.setStatus(AccountStatus.PENDING_VERIFICATION);
    
    // When & Then
    SendByOpException exception = assertThrows(SendByOpException.class, 
        () -> authService.validateAccountStatus(user));
    
    assertEquals(ErrorInfo.EMAIL_NOT_VERIFIED, exception.getErrorInfo());
}
```

**2. Implémentation (GREEN)**
```java
private void validateAccountStatus(User user) throws SendByOpException {
    if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
        throw new SendByOpException(ErrorInfo.EMAIL_NOT_VERIFIED);
    }
    if (user.getStatus() == AccountStatus.BLOCKED) {
        throw new SendByOpException(ErrorInfo.ACCOUNT_BLOCKED);
    }
    if (user.getStatus() == AccountStatus.INACTIVE) {
        throw new SendByOpException(ErrorInfo.ACCOUNT_INACTIVE);
    }
}
```

---

## 🔧 Configuration JUnit 5

### Dépendances pom.xml

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ (assertions fluides) -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 📖 Ressources

### Livres
- **Test Driven Development: By Example** - Kent Beck
- **Growing Object-Oriented Software, Guided by Tests** - Freeman & Pryce

### Articles
- [Martin Fowler - Test Driven Development](https://martinfowler.com/bliki/TestDrivenDevelopment.html)
- [Spring Boot Testing Best Practices](https://spring.io/guides/gs/testing-web/)

### Outils
- **JUnit 5:** Framework de test
- **Mockito:** Mocking framework
- **AssertJ:** Assertions fluides
- **Jacoco:** Couverture de code
- **ArchUnit:** Tests d'architecture

---

## 🎯 Résumé

**Règle d'Or:** ✅ **Tests AVANT Code**

1. **RED:** Écrire un test qui échoue
2. **GREEN:** Faire passer le test avec le code minimal
3. **REFACTOR:** Améliorer sans casser les tests

**Avantages TDD:**
- ✅ Code mieux conçu
- ✅ Bugs détectés tôt
- ✅ Refactoring sécurisé
- ✅ Documentation vivante (tests = specs)
- ✅ Confiance dans le code

**Pour SendByOp, appliquer TDD sur:**
- Nouvelles fonctionnalités
- Corrections de bugs (test de régression)
- Refactoring majeur
- API critiques (paiement, réservation)
