# Système Multi-Photos pour les Colis

## 🎯 Objectif
Permettre aux clients d'uploader plusieurs photos par colis pour donner aux voyageurs une meilleure visibilité sur le contenu à transporter.

## ✅ Ce qui a été implémenté

### 1. **Nouvelle entité `ParcelPhoto`**
**Fichier:** `src/main/java/com/sendByOP/expedition/models/entities/ParcelPhoto.java`

**Fonctionnalités:**
- ✅ Stockage de multiples photos par réservation
- ✅ URL de la photo
- ✅ Description optionnelle
- ✅ Ordre d'affichage (`displayOrder`)
- ✅ Marqueur photo principale (`isPrimary`)
- ✅ Relation `@ManyToOne` vers `Booking`
- ✅ Extends `BaseEntity` (timestamps automatiques)

### 2. **Entité `Booking` modifiée**
**Fichier:** `src/main/java/com/sendByOP/expedition/models/entities/Booking.java`

**Changements:**
- ✅ Nouvelle relation `@OneToMany` vers `ParcelPhoto`
- ✅ Tri automatique par `displayOrder` et `id`
- ✅ `CascadeType.ALL` + `orphanRemoval = true`
- ⚠️ Ancien champ `parcelPhotoUrl` marqué `@Deprecated` (compatibilité)

### 3. **DTO `ParcelPhotoDto`**
**Fichier:** `src/main/java/com/sendByOP/expedition/models/dto/ParcelPhotoDto.java`

**Contenu:**
- ID, photoUrl, description, displayOrder, isPrimary

### 4. **DTO `BookingResponseDto` enrichi**
**Fichier:** `src/main/java/com/sendByOP/expedition/models/dto/BookingResponseDto.java`

**Ajouts:**
- ✅ `List<ParcelPhotoDto> parcelPhotos`
- ⚠️ `parcelPhotoUrl` marqué `@Deprecated`

### 5. **Contrôleur `BookingController` mis à jour**
**Fichier:** `src/main/java/com/sendByOP/expedition/web/controller/BookingController.java`

**Endpoint modifié:**
```java
POST /api/bookings
@RequestParam("parcelPhotos") MultipartFile[] parcelPhotos  // Tableau au lieu d'un fichier unique
```

## 📋 Ce qu'il reste à faire

### 🔴 **PRIORITÉ 1 - Logique métier dans `BookingService`**

**Fichier à modifier:** `src/main/java/com/sendByOP/expedition/services/impl/BookingService.java`

#### **Méthode à créer/modifier: `createBooking()`**

```java
public BookingResponseDto createBooking(
    CreateBookingRequest request, 
    MultipartFile[] parcelPhotos,  // <- Changement ici (tableau)
    Integer customerId
) throws SendByOpException {
    
    // 1. Valider le nombre de photos (recommandé: 1-5)
    if (parcelPhotos == null || parcelPhotos.length == 0) {
        throw new SendByOpException(ErrorInfo.INVALID_DATA, "Au moins une photo est requise");
    }
    
    if (parcelPhotos.length > 5) {
        throw new SendByOpException(ErrorInfo.INVALID_DATA, "Maximum 5 photos autorisées");
    }
    
    // 2. Créer la réservation (logique existante)
    Booking booking = ... // Logique actuelle
    
    // 3. Uploader et sauvegarder chaque photo
    List<ParcelPhoto> photos = new ArrayList<>();
    for (int i = 0; i < parcelPhotos.length; i++) {
        MultipartFile file = parcelPhotos[i];
        
        // Valider et uploader via FileStorageService
        String photoUrl = fileStorageService.storeParcelPhoto(file);
        
        // Créer l'entité ParcelPhoto
        ParcelPhoto photo = ParcelPhoto.builder()
                .photoUrl(photoUrl)
                .displayOrder(i)
                .isPrimary(i == 0)  // La première est la principale
                .booking(booking)
                .build();
                
        photos.add(photo);
    }
    
    // 4. Associer les photos à la réservation
    booking.setParcelPhotos(photos);
    
    // 5. Pour compatibilité, set la première photo comme URL principale
    if (!photos.isEmpty()) {
        booking.setParcelPhotoUrl(photos.get(0).getPhotoUrl());
    }
    
    // 6. Sauvegarder (cascade sauvera automatiquement les photos)
    Booking saved = bookingRepository.save(booking);
    
    // 7. Mapper vers DTO avec les photos
    return mapToResponseDto(saved);
}
```

### 🟡 **PRIORITÉ 2 - Extension de `FileStorageService`**

**Fichier:** `src/main/java/com/sendByOP/expedition/services/impl/FileStorageService.java`

#### **Nouvelle méthode à ajouter:**

```java
/**
 * Stocke une photo de colis avec validation
 * 
 * @param file Fichier image
 * @return URL de la photo stockée
 * @throws SendByOpException Si validation échoue
 */
public String storeParcelPhoto(MultipartFile file) throws SendByOpException {
    // Réutiliser la logique existante de validateImage()
    validateImage(file);
    
    // Générer nom unique
    String fileName = UUID.randomUUID().toString() + getFileExtension(file);
    
    // Chemin: uploads/parcels/yyyy/MM/fileName.jpg
    String directory = "uploads/parcels/" + 
                       LocalDate.now().getYear() + "/" + 
                       LocalDate.now().getMonthValue();
    
    // Créer répertoires si nécessaire
    Path dirPath = Paths.get(directory);
    Files.createDirectories(dirPath);
    
    // Sauvegarder le fichier
    Path targetPath = dirPath.resolve(fileName);
    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    
    // Retourner l'URL relative
    return "/" + directory + "/" + fileName;
}
```

### 🟡 **PRIORITÉ 3 - Mapper dans `mapToResponseDto()`**

Ajouter le mapping des photos dans la méthode qui convertit `Booking` → `BookingResponseDto`:

```java
private BookingResponseDto mapToResponseDto(Booking booking) {
    // ... mapping existant ...
    
    // Mapper les photos
    List<ParcelPhotoDto> photoDtos = booking.getParcelPhotos().stream()
        .map(photo -> ParcelPhotoDto.builder()
            .id(photo.getId())
            .photoUrl(photo.getPhotoUrl())
            .description(photo.getDescription())
            .displayOrder(photo.getDisplayOrder())
            .isPrimary(photo.getIsPrimary())
            .build())
        .collect(Collectors.toList());
    
    return BookingResponseDto.builder()
        // ... autres champs ...
        .parcelPhotos(photoDtos)
        .parcelPhotoUrl(booking.getParcelPhotoUrl())  // Compatibilité
        .build();
}
```

### 🟢 **PRIORITÉ 4 - Migration de base de données**

**Créer le script SQL:** `src/main/resources/db/migration/V1__add_parcel_photos_table.sql`

```sql
-- Table pour les photos de colis
CREATE TABLE parcel_photo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE,
    INDEX idx_photo_booking (booking_id)
);

-- Migrer les photos existantes depuis booking.parcel_photo_url
INSERT INTO parcel_photo (booking_id, photo_url, display_order, is_primary)
SELECT id, parcel_photo_url, 0, TRUE
FROM booking
WHERE parcel_photo_url IS NOT NULL;
```

## 🔧 Configuration requise

### **application-file-storage.properties**

Ajouter les configurations pour les photos de colis :

```properties
# Limites pour photos de colis
app.file.parcel-photos.max-file-size=5MB
app.file.parcel-photos.max-files=5
app.file.parcel-photos.allowed-types=image/jpeg,image/png,image/webp
app.file.parcel-photos.upload-dir=uploads/parcels
```

## 📱 Utilisation de l'API

### **Créer une réservation avec plusieurs photos**

```http
POST /api/bookings
Content-Type: multipart/form-data

// Form data:
flightId: 123
receiverFirstName: Jean
receiverLastName: Dupont
receiverEmail: jean@example.com
parcelWeight: 5.5
parcelDescription: Vêtements
customerId: 456

// Files (array):
parcelPhotos[0]: photo1.jpg
parcelPhotos[1]: photo2.jpg
parcelPhotos[2]: photo3.jpg
```

### **Réponse**

```json
{
  "id": 789,
  "status": "PENDING_CONFIRMATION",
  "parcelPhotos": [
    {
      "id": 1,
      "photoUrl": "/uploads/parcels/2025/10/uuid-1.jpg",
      "isPrimary": true,
      "displayOrder": 0
    },
    {
      "id": 2,
      "photoUrl": "/uploads/parcels/2025/10/uuid-2.jpg",
      "isPrimary": false,
      "displayOrder": 1
    },
    {
      "id": 3,
      "photoUrl": "/uploads/parcels/2025/10/uuid-3.jpg",
      "isPrimary": false,
      "displayOrder": 2
    }
  ],
  "parcelPhotoUrl": "/uploads/parcels/2025/10/uuid-1.jpg"  // Deprecated, pour compatibilité
}
```

## ✨ Avantages de cette approche

### **1. Meilleure transparence**
- Voyageurs peuvent voir plusieurs angles du colis
- Réduit les malentendus sur le contenu
- Augmente la confiance

### **2. Flexibilité**
- Support de 1 à 5 photos
- Photo principale automatiquement identifiée
- Ordre d'affichage personnalisable

### **3. Rétrocompatibilité**
- Ancien champ `parcelPhotoUrl` conservé
- APIs existantes continuent de fonctionner
- Migration progressive possible

### **4. Evolutivité**
- Facile d'ajouter descriptions par photo
- Possibilité future : annotations, crop, rotation
- Support futur : vidéos courtes

## 🚀 Prochaines étapes recommandées

1. ✅ **Implémenter la logique dans `BookingService`**
2. ✅ **Étendre `FileStorageService`**
3. ✅ **Créer et exécuter la migration SQL**
4. ✅ **Tester avec Postman/curl**
5. ✅ **Mettre à jour les tests unitaires**
6. ✅ **Documenter dans Swagger**

## 📊 Limites recommandées

| Paramètre | Valeur | Raison |
|-----------|--------|--------|
| **Min photos** | 1 | Au moins une vue du colis |
| **Max photos** | 5 | Éviter surcharge serveur/réseau |
| **Taille max/photo** | 5 MB | Qualité suffisante, upload rapide |
| **Formats** | JPEG, PNG, WebP | Standards web |
| **Résolution max** | 4096x4096 | Limite raisonnable |

## ⚠️ Points d'attention

### **Sécurité**
- ✅ Validation stricte des types MIME
- ✅ Vérification du contenu réel (pas juste extension)
- ✅ Noms de fichiers sécurisés (UUID)
- ✅ Protection contre path traversal

### **Performance**
- Uploader les photos en parallèle si possible
- Créer des thumbnails pour liste/aperçu
- CDN pour servir les images en production

### **Stockage**
- Organiser par date (yyyy/MM) pour éviter trop de fichiers/répertoire
- Implémenter nettoyage des photos orphelines
- Considérer compression automatique

---

**Statut:** 🟡 En cours d'implémentation  
**Date:** 26 octobre 2025  
**Auteur:** Cascade AI Assistant
