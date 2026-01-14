# 🚀 Quick Start - SendByOp Refactoring

**Pour démarrer rapidement après cette session**

---

## ✅ Ce Qui Est Fait (30%)

```
✅ Sprint 1: Infrastructure (énums, entités, migrations)
✅ Sprint 2a: ReceiverService (13 tests)
✅ Sprint 2b: PlatformSettingsService (10 tests)
```

---

## 🎯 Première Utilisation

### 1️⃣ Compiler le Projet (2 min)

```powershell
# Nettoyer et compiler
.\mvnw.cmd clean compile
```

**Résultat attendu:** `BUILD SUCCESS`

---

### 2️⃣ Appliquer les Migrations (5 min)

```powershell
# IMPORTANT: Backup d'abord !
mysqldump -u root -p sendbyop > backup.sql

# Appliquer V4-V8
.\mvnw.cmd flyway:migrate

# Vérifier
.\mvnw.cmd flyway:info
```

**Résultat attendu:** 5 migrations en status `SUCCESS`

---

### 3️⃣ Exécuter les Tests (3 min)

```powershell
# Utiliser le script fourni
.\run-tests.ps1 all

# Ou manuellement
.\mvnw.cmd test -Dtest=ReceiverServiceTest
.\mvnw.cmd test -Dtest=PlatformSettingsServiceTest
```

**Résultat attendu:** `23 tests passent` (13 + 10)

---

### 4️⃣ Démarrer l'Application (1 min)

```powershell
# Démarrer
.\mvnw.cmd spring-boot:run

# Vérifier (dans un autre terminal)
curl http://localhost:9002/actuator/health
```

**Résultat attendu:** `{"status":"UP"}`

---

## 📚 Documentation Essentielle

| Fichier | Contenu |
|---------|---------|
| `SESSION_RECAP_20251023.md` | 📊 **Récapitulatif complet** de la session |
| `NEXT_STEPS.md` | 🎯 **Prochaines actions** détaillées |
| `SPRINT2_PHASE1_SUMMARY.md` | 🔍 ReceiverService en détail |
| `SPRINT2B_SUMMARY.md` | ⚙️ PlatformSettingsService en détail |
| `MIGRATIONS_SUMMARY.md` | 🗄️ Guide des migrations SQL |

---

## 🧪 Tests Rapides

### Option 1: Script Automatique
```powershell
.\run-tests.ps1 all         # Tous les tests
.\run-tests.ps1 receiver    # ReceiverService uniquement
.\run-tests.ps1 settings    # PlatformSettingsService uniquement
.\run-tests.ps1 compile     # Compilation uniquement
.\run-tests.ps1 migrations  # Vérifier migrations
```

### Option 2: Maven Direct
```powershell
.\mvnw.cmd test                              # Tous
.\mvnw.cmd test -Dtest=ReceiverServiceTest   # 13 tests
.\mvnw.cmd test -Dtest=PlatformSettingsServiceTest # 10 tests
```

---

## 🔧 API Admin

### Endpoints Disponibles
```
GET  /api/admin/settings        # Récupérer paramètres
PUT  /api/admin/settings        # Mettre à jour
POST /api/admin/settings/reset  # Réinitialiser
```

### Test avec curl (après auth)
```bash
# 1. Authentification
curl -X POST http://localhost:9002/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin"}'

# 2. Récupérer token dans la réponse

# 3. Utiliser l'API
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:9002/api/admin/settings
```

---

## 🐛 Problèmes Courants

### Erreurs IDE (String cannot be resolved)
```powershell
# Solution 1: Nettoyer
Remove-Item -Recurse -Force target
.\mvnw.cmd clean compile

# Solution 2: IntelliJ
File → Invalidate Caches / Restart
```

### Tests Échouent
```powershell
# Vérifier migrations
.\mvnw.cmd flyway:info

# Si besoin, appliquer
.\mvnw.cmd flyway:migrate
```

### Base de Données Inaccessible
```yaml
# Vérifier application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/sendbyop
spring.datasource.username=root
spring.datasource.password=votre_password
```

---

## 📊 Structure du Projet

```
src/main/java/com/sendByOP/expedition/
├── models/
│   ├── enums/                    ✨ 4 nouvelles énumérations
│   ├── entities/                 ✨ 5 entités modifiées/créées
│   └── dto/                      ✨ 2 nouveaux DTOs
├── repositories/                 ✨ 2 nouveaux repos
├── mappers/                      ✨ 2 nouveaux mappers
├── services/
│   ├── iServices/                ✨ 2 nouvelles interfaces
│   └── impl/                     ✨ 2 nouveaux services
├── controllers/
│   └── admin/                    ✨ 1 nouveau controller
└── exception/                    ✏️ ErrorInfo enrichi

src/main/resources/
└── db/migration/                 ✨ 5 nouvelles migrations

src/test/java/
└── services/                     ✨ 23 nouveaux tests
```

---

## 🎯 Prochaine Étape: Sprint 2c

**BookingService - Création de réservation**

**Composants à créer:**
- CreateBookingRequest DTO
- BookingService avec createBooking()
- BookingServiceTest (TDD)
- BookingController avec POST /api/bookings

**Durée:** 4-5 heures

**Voir:** `NEXT_STEPS.md` pour le détail complet

---

## 💡 Commandes Utiles

```powershell
# Compilation
.\mvnw.cmd clean compile

# Tests
.\mvnw.cmd test
.\run-tests.ps1 all

# Migrations
.\mvnw.cmd flyway:info
.\mvnw.cmd flyway:migrate

# Démarrage
.\mvnw.cmd spring-boot:run

# Coverage
.\mvnw.cmd test jacoco:report

# Package
.\mvnw.cmd package -DskipTests
```

---

## 📞 Aide & Support

**Documentation:**
- Tous les `*.md` à la racine du projet
- Tests comme exemples (`*Test.java`)
- Commentaires Javadoc dans le code

**Logs:**
```powershell
# Voir les logs détaillés
.\mvnw.cmd test -X
.\mvnw.cmd spring-boot:run -X
```

---

## ✨ Checklist de Démarrage

```
[ ] Projet compilé avec succès
[ ] Migrations appliquées (V4-V8)
[ ] 23 tests passent (13 + 10)
[ ] Application démarre
[ ] API /actuator/health retourne UP
[ ] Documentation lue (SESSION_RECAP_20251023.md)
```

---

**Tout est prêt ! Bonne continuation ! 🚀**
