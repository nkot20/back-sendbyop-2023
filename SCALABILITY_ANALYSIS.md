# Analyse de Scalabilité - SendByOp Backend

**Date:** 23 octobre 2025  
**Version:** 2.0.0  
**Framework:** Spring Boot 3.1.4 + Java 17

---

## 📊 Capacité Estimée Actuelle

### Estimation Réaliste

**Configuration Actuelle (Sans Optimisation):**
- **Utilisateurs simultanés:** ~100-200 utilisateurs
- **Utilisateurs totaux:** ~10,000-50,000 utilisateurs
- **Requêtes/seconde:** ~50-100 req/s
- **Temps de réponse moyen:** 200-500ms

**Avec Optimisations Basiques:**
- **Utilisateurs simultanés:** ~500-1,000 utilisateurs
- **Utilisateurs totaux:** ~100,000-500,000 utilisateurs
- **Requêtes/seconde:** ~200-500 req/s
- **Temps de réponse moyen:** 100-200ms

---

## 🔍 Analyse Technique

### Architecture Actuelle

```
┌─────────────────────────────────────────────────────┐
│                   FRONTEND                           │
│            (React/Angular/Vue)                       │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP/REST
┌──────────────────▼──────────────────────────────────┐
│              Spring Boot Backend                     │
│  ┌────────────────────────────────────────────┐     │
│  │  Controllers (REST API)                    │     │
│  └──────────────┬─────────────────────────────┘     │
│  ┌──────────────▼─────────────────────────────┐     │
│  │  Services (Business Logic)                 │     │
│  │  - 33 Services @Transactional              │     │
│  └──────────────┬─────────────────────────────┘     │
│  ┌──────────────▼─────────────────────────────┐     │
│  │  Repositories (JPA/Hibernate)              │     │
│  └──────────────┬─────────────────────────────┘     │
└─────────────────┼───────────────────────────────────┘
                  │
    ┌─────────────┼──────────────┐
    │             │              │
┌───▼───┐    ┌───▼────┐    ┌───▼─────┐
│ MySQL │    │ Redis  │    │ Twilio  │
│  ou   │    │ Cache  │    │  SMS    │
│ PostgreSQL│    │        │    │         │
└───────┘    └────────┘    └─────────┘
```

---

## ⚙️ Points Forts Actuels

### 1. ✅ Cache Redis Configuré
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=600000  # 10 minutes
```

**Impact:**
- Réduction de ~80% des requêtes BD pour données fréquemment consultées
- Amélioration temps de réponse pour listes (vols, aéroports)

### 2. ✅ Gestion Transactionnelle
- 28 services avec `@Transactional`
- Cohérence des données garantie
- Rollback automatique en cas d'erreur

### 3. ✅ JWT Authentication
- Authentification stateless (scalable horizontalement)
- Pas de session côté serveur
- Token expirant (15min prod, 24h dev)

### 4. ✅ MapStruct (Mappings Optimisés)
- Conversion DTO ↔ Entity à la compilation
- Zéro réflexion runtime
- Performance maximale

### 5. ✅ Chiffrement AES-256-GCM
- Données bancaires sécurisées
- Impact performance minime (~5-10ms par opération)

---

## ⚠️ Goulots d'Étranglement Actuels

### 1. 🔴 CRITIQUE - Configuration Base de Données

**Problème:** Aucune configuration Hikari explicite

```properties
# ❌ MANQUANT - Configuration par défaut sous-optimale
# spring.datasource.hikari.maximum-pool-size=10 (défaut)
# spring.datasource.hikari.minimum-idle=10
# spring.datasource.hikari.connection-timeout=20000
```

**Impact:**
- Pool de connexions limité à 10 (défaut)
- Contention à partir de ~50 utilisateurs simultanés
- Timeouts fréquents sous charge

**Solution Recommandée:**
```properties
# Configuration Optimisée Hikari
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000
```

### 2. 🟠 IMPORTANT - Configuration Tomcat

**Problème:** Aucune configuration threads Tomcat

```properties
# ❌ MANQUANT - Défauts: 200 threads max
# server.tomcat.threads.max=200
# server.tomcat.threads.min-spare=10
```

**Impact:**
- Limitation à ~200 requêtes simultanées
- Queue limitée (défaut: 100)

**Solution Recommandée:**
```properties
# Configuration Tomcat
server.tomcat.threads.max=400
server.tomcat.threads.min-spare=50
server.tomcat.max-connections=10000
server.tomcat.accept-count=200
server.tomcat.connection-timeout=20000
```

### 3. 🟠 Problème N+1 Queries (Probable)

**Services Analysés:** 33 services transactionnels

**Risques Identifiés:**
- `FlightService.java` (14.4 KB) - Relations Flight → Customer, Airports, Stopovers
- `ReservationService.java` (21.2 KB) - Relations complexes Booking → Parcel, Customer, Flight
- `CustomerService.java` - Relations Customer → Flights, Bookings

**Impact Estimé:**
- 1 requête de liste peut générer N+1 requêtes BD
- Exemple: 100 vols = 1 + 100*3 = 301 requêtes au lieu de 4

**Solution:**
```java
// Utiliser fetch joins
@Query("SELECT f FROM Flight f " +
       "JOIN FETCH f.customer " +
       "JOIN FETCH f.departureAirport " +
       "JOIN FETCH f.arrivalAirport " +
       "WHERE f.validationStatus = :status")
List<Flight> findByValidationStatusWithDetails(@Param("status") int status);
```

### 4. 🟡 Cache Redis Non Utilisé

**Problème:** Cache configuré mais pas d'annotations `@Cacheable`

```bash
# Résultat grep: 0 occurrences de @Cacheable
```

**Impact:**
- Redis configuré mais inutilisé
- Toutes les requêtes vont en BD

**Solution:**
```java
@Cacheable(value = "flights", key = "#id")
public FlightDto getVolById(int id) { ... }

@Cacheable(value = "airports", unless = "#result.isEmpty()")
public List<AirportDto> getAllAirports() { ... }

@CacheEvict(value = "flights", key = "#flightDto.id")
public FlightDto updateFlight(FlightDto flightDto) { ... }
```

### 5. 🟡 JPA Show SQL en Production

**Problème (Dev):**
```properties
spring.jpa.show-sql=true  # En dev - OK
```

**Problème (Prod):**
```properties
spring.jpa.show-sql=false  # En prod - BIEN mais pas de logs perfs
```

**Solution:**
```properties
# Production - Logs optimisés
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
logging.level.org.hibernate.SQL=INFO
```

### 6. 🟡 Pas de Pagination Généralisée

**Services Sans Pagination:**
- `getAllVol()` - Retourne TOUS les vols
- `getAllAirports()` - Retourne TOUS les aéroports
- `getListClient()` - Retourne TOUS les clients

**Impact:**
- Avec 10,000 vols → 10,000 objets en mémoire → OutOfMemoryError
- Temps de réponse exponentiel

**Solution:**
```java
// Pagination Spring Data
Page<Flight> findAll(Pageable pageable);

// Usage
Pageable pageable = PageRequest.of(page, size, Sort.by("departureDate").descending());
Page<FlightDto> flights = flightService.getAllVol(pageable);
```

### 7. 🟡 Chiffrement Synchrone

**Fichier:** `EncryptionService.java`

**Impact:**
- Chiffrement/déchiffrement bloquant
- ~5-10ms par opération
- Ralentit les opérations bancaires

**Solution (Optimisation Future):**
```java
@Async
public CompletableFuture<String> encryptAsync(String data) {
    return CompletableFuture.completedFuture(encrypt(data));
}
```

---

## 📈 Estimations de Charge Détaillées

### Scénario 1: Configuration Actuelle (Défaut)

**Limites:**
- Pool BD: 10 connexions
- Threads Tomcat: 200
- Pas de cache actif
- N+1 queries probables

**Capacité:**
```
Utilisateurs simultanés: 100
├─ 50% lecture (50 users)   → OK
├─ 30% écriture (30 users)  → Ralentissements
└─ 20% calculs (20 users)   → Timeouts possibles

Requêtes/seconde: 50-100 req/s
Temps réponse moyen: 300-800ms
Taux erreur: 5-10% (timeouts, connexions pool épuisé)
```

### Scénario 2: Optimisations Basiques

**Changements:**
```properties
# Hikari
spring.datasource.hikari.maximum-pool-size=50

# Tomcat
server.tomcat.threads.max=400

# Cache Redis activé sur listes
@Cacheable sur getAllFlights, getAllAirports, etc.
```

**Capacité:**
```
Utilisateurs simultanés: 500-1,000
├─ 70% lecture (700 users)   → Très rapide (cache)
├─ 20% écriture (200 users)  → Fluide
└─ 10% calculs (100 users)   → OK

Requêtes/seconde: 300-500 req/s
Temps réponse moyen: 100-200ms
Taux erreur: <1%
```

### Scénario 3: Optimisations Avancées

**Changements:**
- Hikari: 100 connexions
- Tomcat: 600 threads
- Cache Redis complet
- Fetch joins (pas de N+1)
- Pagination généralisée
- Index BD optimisés
- Connection pooling Redis

**Capacité:**
```
Utilisateurs simultanés: 2,000-5,000
├─ 80% lecture (4,000 users)  → Ultra-rapide (cache)
├─ 15% écriture (750 users)   → Rapide
└─ 5% calculs (250 users)     → Fluide

Requêtes/seconde: 1,000-2,000 req/s
Temps réponse moyen: 50-100ms
Taux erreur: <0.1%
```

### Scénario 4: Architecture Microservices

**Pour >10,000 utilisateurs simultanés:**
- Load balancer (Nginx/HAProxy)
- Plusieurs instances Spring Boot (horizontale scaling)
- BD read replicas (master-slave)
- Redis Cluster
- CDN pour assets
- Message Queue (RabbitMQ/Kafka)

---

## 🎯 Plan d'Action Recommandé

### Phase 1: Quick Wins (1-2 jours) 🚀

**Priorité CRITIQUE:**

1. **Configuration Hikari**
   ```properties
   spring.datasource.hikari.maximum-pool-size=50
   spring.datasource.hikari.minimum-idle=10
   ```

2. **Configuration Tomcat**
   ```properties
   server.tomcat.threads.max=400
   server.tomcat.threads.min-spare=50
   ```

3. **Activation Cache Redis**
   ```java
   @Cacheable(value = "flights")
   public List<FlightDto> getAllVol() { ... }
   ```

**Gain Estimé:**
- Capacité: 100 → 500 utilisateurs simultanés (+400%)
- Temps réponse: -60%
- Taux erreur: -80%

### Phase 2: Optimisations BD (3-5 jours) 📊

1. **Ajouter Fetch Joins**
   - `FlightService` - JOIN FETCH customer, airports
   - `ReservationService` - JOIN FETCH flight, parcel

2. **Créer Index BD**
   ```sql
   CREATE INDEX idx_flight_status ON flight(validation_status);
   CREATE INDEX idx_flight_date ON flight(departure_date);
   CREATE INDEX idx_booking_customer ON booking(customer_id);
   CREATE INDEX idx_user_email ON user(email);
   ```

3. **Ajouter Pagination**
   - Toutes les méthodes `getAll*()`
   - Limite par défaut: 20 items

**Gain Estimé:**
- Requêtes BD: -80%
- Temps réponse: -40% supplémentaire
- Capacité BD: x10

### Phase 3: Monitoring (2-3 jours) 📈

1. **Actuator + Prometheus**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-registry-prometheus</artifactId>
   </dependency>
   ```

2. **Métriques Custom**
   ```java
   @Timed(value = "flight.creation", histogram = true)
   public FlightDto saveVol(FlightDto dto) { ... }
   ```

3. **Alertes**
   - Pool BD > 80% usage
   - Temps réponse > 500ms
   - Taux erreur > 1%

### Phase 4: Load Testing (1 semaine) 🧪

1. **JMeter / Gatling**
   ```scala
   // Gatling scenario
   scenario("User Journey")
     .exec(http("Login").post("/auth/login"))
     .pause(2)
     .exec(http("Get Flights").get("/flights"))
     .pause(5)
     .exec(http("Create Booking").post("/bookings"))
   ```

2. **Objectifs Tests:**
   - 500 utilisateurs simultanés
   - 1,000 req/s pendant 10min
   - <200ms p95 temps réponse
   - <1% taux erreur

---

## 🏗️ Architecture Cible (Scalabilité Maximale)

### Pour 10,000+ Utilisateurs Simultanés

```
                    ┌─────────────┐
                    │ Load Balancer│
                    │   (Nginx)   │
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼───┐        ┌────▼───┐       ┌────▼───┐
    │ Backend│        │ Backend│       │ Backend│
    │Instance│        │Instance│       │Instance│
    │   #1   │        │   #2   │       │   #3   │
    └────┬───┘        └────┬───┘       └────┬───┘
         │                 │                 │
         └─────────────────┼─────────────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
         ┌────▼────┐  ┌────▼────┐ ┌────▼────┐
         │  Master │  │ Replica │ │ Replica │
         │   MySQL │  │  MySQL  │ │  MySQL  │
         │  (Write)│  │  (Read) │ │  (Read) │
         └─────────┘  └─────────┘ └─────────┘
              │
         ┌────▼────┐
         │  Redis  │
         │ Cluster │
         └─────────┘
```

**Capacité Estimée:**
- **50,000+ utilisateurs simultanés**
- **10,000+ req/s**
- **<50ms temps réponse moyen**
- **99.9% uptime**

---

## 📊 Tableau Récapitulatif

| Configuration | Users Simultanés | Total Users | Req/s | Temps Réponse | Coût Infra/mois |
|--------------|------------------|-------------|-------|---------------|-----------------|
| **Actuelle (Défaut)** | 100-200 | 10K-50K | 50-100 | 300-800ms | $20-50 (1 serveur) |
| **Optimisée (Phase 1+2)** | 500-1,000 | 100K-500K | 300-500 | 100-200ms | $50-100 (1 serveur + Redis) |
| **Avancée (Phase 3+4)** | 2,000-5,000 | 500K-1M | 1,000-2,000 | 50-100ms | $200-400 (2-3 serveurs) |
| **Microservices** | 10,000-50,000 | 1M-10M | 5,000-10,000 | <50ms | $1,000-5,000 (cluster) |

---

## ✅ Checklist d'Optimisation

### Immédiat (Aujourd'hui)
- [ ] Configurer Hikari pool (50 connexions)
- [ ] Configurer Tomcat threads (400 threads)
- [ ] Activer cache Redis sur listes

### Court Terme (Cette Semaine)
- [ ] Ajouter fetch joins (FlightService, ReservationService)
- [ ] Créer index BD critiques
- [ ] Ajouter pagination sur méthodes getAll

### Moyen Terme (Ce Mois)
- [ ] Implémenter monitoring (Actuator, Prometheus)
- [ ] Load testing avec JMeter/Gatling
- [ ] Optimiser requêtes lentes (>100ms)

### Long Terme (Prochain Trimestre)
- [ ] Architecture microservices si >5,000 users
- [ ] Redis Cluster pour haute disponibilité
- [ ] BD read replicas
- [ ] CDN pour assets statiques

---

## 🎯 Conclusion

### Capacité Actuelle
**Estimation Conservative:** ~**100-200 utilisateurs simultanés**
**Estimation Optimiste (avec cache existant):** ~**300-500 utilisateurs**

### Avec Optimisations Phase 1+2
**Capacité Réaliste:** ~**1,000-2,000 utilisateurs simultanés**
**Soit ~100,000-500,000 utilisateurs totaux**

### Recommandation
**Action Immédiate:** Implémenter Phase 1 (Quick Wins) avant mise en production.

**ROI:**
- Investissement: 1-2 jours dev
- Gain: +400% capacité
- Coût: $0 (juste configuration)

---

**Note:** Ces estimations sont basées sur une analyse statique du code. Un **load testing réel** est indispensable pour des chiffres précis.
