package com.sendByOP.expedition.services;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.ReceiverDto;
import com.sendByOP.expedition.models.entities.Receiver;
import com.sendByOP.expedition.models.enums.RecipientStatus;
import com.sendByOP.expedition.repositories.ReceiverRepository;
import com.sendByOP.expedition.services.impl.ReceiverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ReceiverService
 * Approche TDD: Tests écrits AVANT l'implémentation
 */
@SpringBootTest
@Transactional
@DisplayName("ReceiverService Tests")
class ReceiverServiceTest {

    @Autowired
    private ReceiverService receiverService;

    @Autowired
    private ReceiverRepository receiverRepository;

    private ReceiverDto validReceiverDto;

    @BeforeEach
    void setUp() {
        // Nettoyer la base avant chaque test
        receiverRepository.deleteAll();

        // Créer un DTO valide pour les tests
        validReceiverDto = ReceiverDto.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@example.com")
                .phoneNumber("+33612345678")
                .address("123 Rue de la Paix")
                .city("Paris")
                .country("France")
                .build();
    }

    // ==========================================
    // TEST 1: Création avec données valides
    // ==========================================
    @Test
    @DisplayName("Devrait créer un destinataire avec des données valides")
    void shouldCreateReceiverWithValidData() throws SendByOpException {
        // When
        ReceiverDto created = receiverService.createReceiver(validReceiverDto);

        // Then
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Jean", created.getFirstName());
        assertEquals("Dupont", created.getLastName());
        assertEquals("jean.dupont@example.com", created.getEmail());
        assertEquals("+33612345678", created.getPhoneNumber());
        assertEquals(RecipientStatus.ACTIVE, created.getStatus());
        assertNotNull(created.getCreatedAt());
    }

    // ==========================================
    // TEST 2: Récupération par email existant
    // ==========================================
    @Test
    @DisplayName("Devrait récupérer un destinataire existant par email")
    void shouldGetExistingReceiverByEmail() throws SendByOpException {
        // Given
        ReceiverDto created = receiverService.createReceiver(validReceiverDto);

        // When
        ReceiverDto found = receiverService.getReceiverByEmail("jean.dupont@example.com");

        // Then
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("jean.dupont@example.com", found.getEmail());
    }

    // ==========================================
    // TEST 3: Récupération par téléphone existant
    // ==========================================
    @Test
    @DisplayName("Devrait récupérer un destinataire existant par numéro de téléphone")
    void shouldGetExistingReceiverByPhoneNumber() throws SendByOpException {
        // Given
        ReceiverDto created = receiverService.createReceiver(validReceiverDto);

        // When
        ReceiverDto found = receiverService.getReceiverByPhoneNumber("+33612345678");

        // Then
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("+33612345678", found.getPhoneNumber());
    }

    // ==========================================
    // TEST 4: GetOrCreate - Cas création
    // ==========================================
    @Test
    @DisplayName("Devrait créer un destinataire quand il n'existe pas")
    void shouldCreateReceiverWhenNotExists() throws SendByOpException {
        // When
        ReceiverDto result = receiverService.getOrCreateReceiver(validReceiverDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("jean.dupont@example.com", result.getEmail());

        // Vérifier qu'il existe bien en BD
        assertTrue(receiverRepository.existsByEmail("jean.dupont@example.com"));
    }

    // ==========================================
    // TEST 5: GetOrCreate - Cas récupération par email
    // ==========================================
    @Test
    @DisplayName("Devrait récupérer le destinataire existant quand l'email existe")
    void shouldRetrieveReceiverWhenEmailExists() throws SendByOpException {
        // Given
        ReceiverDto created = receiverService.createReceiver(validReceiverDto);

        // When - Essayer de créer avec même email mais différent téléphone
        ReceiverDto newRequest = ReceiverDto.builder()
                .firstName("Marie")
                .lastName("Martin")
                .email("jean.dupont@example.com") // Même email
                .phoneNumber("+33698765432") // Différent téléphone
                .build();

        ReceiverDto result = receiverService.getOrCreateReceiver(newRequest);

        // Then - Devrait retourner l'existant, pas créer nouveau
        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("Jean", result.getFirstName()); // Prénom original, pas "Marie"
        assertEquals("jean.dupont@example.com", result.getEmail());

        // Vérifier qu'un seul destinataire existe
        assertEquals(1, receiverRepository.count());
    }

    // ==========================================
    // TEST 6: GetOrCreate - Cas récupération par téléphone
    // ==========================================
    @Test
    @DisplayName("Devrait récupérer le destinataire existant quand le téléphone existe")
    void shouldRetrieveReceiverWhenPhoneExists() throws SendByOpException {
        // Given
        ReceiverDto created = receiverService.createReceiver(validReceiverDto);

        // When - Essayer avec même téléphone mais différent email
        ReceiverDto newRequest = ReceiverDto.builder()
                .firstName("Pierre")
                .lastName("Durand")
                .email("pierre.durand@example.com") // Différent email
                .phoneNumber("+33612345678") // Même téléphone
                .build();

        ReceiverDto result = receiverService.getOrCreateReceiver(newRequest);

        // Then
        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("Jean", result.getFirstName()); // Prénom original
        assertEquals("+33612345678", result.getPhoneNumber());

        // Vérifier qu'un seul destinataire existe
        assertEquals(1, receiverRepository.count());
    }

    // ==========================================
    // TEST 7: Échec si email ET téléphone null
    // ==========================================
    @Test
    @DisplayName("Devrait lever une exception si email et téléphone sont tous deux null")
    void shouldThrowExceptionWhenBothEmailAndPhoneAreNull() {
        // Given
        ReceiverDto invalidDto = ReceiverDto.builder()
                .firstName("Test")
                .lastName("User")
                .email(null)
                .phoneNumber(null)
                .build();

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> receiverService.getOrCreateReceiver(invalidDto)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        assertTrue(exception.getMessage().contains("Email ou téléphone"));
    }

    // ==========================================
    // TEST 8: Échec si email invalide
    // ==========================================
    @Test
    @DisplayName("Devrait lever une exception si l'email est invalide")
    void shouldThrowExceptionWhenEmailInvalid() {
        // Given
        ReceiverDto invalidDto = ReceiverDto.builder()
                .firstName("Test")
                .lastName("User")
                .email("invalid-email") // Format invalide
                .phoneNumber("+33612345678")
                .build();

        // When & Then
        assertThrows(
                SendByOpException.class,
                () -> receiverService.createReceiver(invalidDto)
        );
    }

    // ==========================================
    // TEST 9: Mise à jour destinataire existant
    // ==========================================
    @Test
    @DisplayName("Devrait mettre à jour un destinataire existant")
    void shouldUpdateExistingReceiver() throws SendByOpException {
        // Given
        ReceiverDto created = receiverService.createReceiver(validReceiverDto);

        // When - Modifier l'adresse
        created.setAddress("456 Avenue des Champs");
        created.setCity("Lyon");
        ReceiverDto updated = receiverService.updateReceiver(created);

        // Then
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals("456 Avenue des Champs", updated.getAddress());
        assertEquals("Lyon", updated.getCity());
        assertNotNull(updated.getUpdatedAt());
    }

    // ==========================================
    // TEST 10: Détecter doublon email
    // ==========================================
    @Test
    @DisplayName("Devrait détecter un doublon d'email lors de la création")
    void shouldDetectDuplicateEmail() throws SendByOpException {
        // Given
        receiverService.createReceiver(validReceiverDto);

        // When - Essayer de créer avec même email
        ReceiverDto duplicate = ReceiverDto.builder()
                .firstName("Marie")
                .lastName("Dubois")
                .email("jean.dupont@example.com") // Même email
                .phoneNumber("+33698765432") // Différent téléphone
                .build();

        // Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> receiverService.createReceiver(duplicate)
        );

        assertTrue(exception.getMessage().contains("email") ||
                   exception.getMessage().contains("existe"));
    }

    // ==========================================
    // TEST 11: Détecter doublon téléphone
    // ==========================================
    @Test
    @DisplayName("Devrait détecter un doublon de téléphone lors de la création")
    void shouldDetectDuplicatePhoneNumber() throws SendByOpException {
        // Given
        receiverService.createReceiver(validReceiverDto);

        // When - Essayer de créer avec même téléphone
        ReceiverDto duplicate = ReceiverDto.builder()
                .firstName("Paul")
                .lastName("Bernard")
                .email("paul.bernard@example.com") // Différent email
                .phoneNumber("+33612345678") // Même téléphone
                .build();

        // Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> receiverService.createReceiver(duplicate)
        );

        assertTrue(exception.getMessage().contains("téléphone") ||
                   exception.getMessage().contains("existe"));
    }

    // ==========================================
    // TEST 12: Vérifier l'existence d'un destinataire
    // ==========================================
    @Test
    @DisplayName("Devrait correctement vérifier l'existence d'un destinataire")
    void shouldCheckReceiverExists() throws SendByOpException {
        // Given
        receiverService.createReceiver(validReceiverDto);

        // When & Then
        assertTrue(receiverService.receiverExists(
                "jean.dupont@example.com",
                null
        ));

        assertTrue(receiverService.receiverExists(
                null,
                "+33612345678"
        ));

        assertFalse(receiverService.receiverExists(
                "nonexistent@example.com",
                null
        ));

        assertFalse(receiverService.receiverExists(
                null,
                "+33600000000"
        ));
    }

    // ==========================================
    // TEST 13: Gestion email null lors de getOrCreate
    // ==========================================
    @Test
    @DisplayName("Devrait gérer correctement un email null avec téléphone valide")
    void shouldHandleNullEmailWithValidPhone() throws SendByOpException {
        // Given
        ReceiverDto dto = ReceiverDto.builder()
                .firstName("Sophie")
                .lastName("Leroy")
                .email(null) // Email null
                .phoneNumber("+33612345678") // Téléphone valide
                .build();

        // When
        ReceiverDto result = receiverService.getOrCreateReceiver(dto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNull(result.getEmail());
        assertEquals("+33612345678", result.getPhoneNumber());
    }
}
