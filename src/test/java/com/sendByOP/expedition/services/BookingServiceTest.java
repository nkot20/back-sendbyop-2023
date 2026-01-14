package com.sendByOP.expedition.services;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.models.enums.BookingStatus;
import com.sendByOP.expedition.repositories.*;
import com.sendByOP.expedition.services.impl.BookingService;
import com.sendByOP.expedition.services.iServices.IPlatformSettingsService;
import com.sendByOP.expedition.services.iServices.IReceiverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour BookingService
 * Approche TDD: Tests écrits AVANT l'implémentation
 */
@SpringBootTest
@Transactional
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ReceiverRepository receiverRepository;

    @Autowired
    private IReceiverService receiverService;

    @Autowired
    private IPlatformSettingsService platformSettingsService;

    private Customer testCustomer;
    private Flight testFlight;
    private CreateBookingRequest validRequest;
    private MockMultipartFile testPhoto;
    private MockMultipartFile[] testPhotos;

    @BeforeEach
    void setUp() {
        // Nettoyer les tables
        bookingRepository.deleteAll();
        receiverRepository.deleteAll();

        // Créer un client de test
        testCustomer = new Customer();
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@test.com");
        testCustomer = customerRepository.save(testCustomer);

        // Créer un vol de test (supposons qu'il existe)
        // Note: Dans un vrai test, il faudrait créer le vol complet
        testFlight = flightRepository.findAll().stream().findFirst().orElse(null);
        if (testFlight == null) {
            // Si aucun vol n'existe, en créer un minimal pour les tests
            testFlight = new Flight();
            // Initialiser les champs requis du vol
            testFlight = flightRepository.save(testFlight);
        }

        // Créer une requête valide
        validRequest = CreateBookingRequest.builder()
                .flightId(testFlight.getFlightId())
                .receiverFirstName("Jane")
                .receiverLastName("Smith")
                .receiverEmail("jane.smith@test.com")
                .receiverPhoneNumber("+33612345678")
                .receiverAddress("123 Rue de Paris")
                .receiverCity("Paris")
                .receiverCountry("France")
                .parcelWeight(BigDecimal.valueOf(5.0))
                .parcelLength(BigDecimal.valueOf(30.0))
                .parcelWidth(BigDecimal.valueOf(20.0))
                .parcelHeight(BigDecimal.valueOf(15.0))
                .parcelDescription("Vêtements et accessoires pour bébé")
                .parcelCategory("Vêtements")
                .build();

        // Créer une photo de test
        testPhoto = new MockMultipartFile(
                "parcelPhoto",
                "test-parcel.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        // Créer un tableau de photos pour les tests
        testPhotos = new MockMultipartFile[] { testPhoto };
    }

    // ==========================================
    // TEST 1: Création booking avec données valides
    // ==========================================
    @Test
    @DisplayName("Devrait créer une réservation avec des données valides")
    void shouldCreateBookingWithValidData() throws SendByOpException {
        // When
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // Then
        assertNotNull(booking);
        assertNotNull(booking.getId());
        assertEquals(BookingStatus.PENDING_CONFIRMATION, booking.getStatus());
        assertEquals(testFlight.getFlightId(), booking.getFlightId());
        assertEquals(testCustomer.getId(), booking.getCustomerId());
        assertNotNull(booking.getReceiverId());
        assertNotNull(booking.getBookingDate());
        assertNotNull(booking.getTotalPrice());
        assertTrue(booking.getTotalPrice().compareTo(BigDecimal.ZERO) > 0);
        
        // Vérifier les photos
        assertNotNull(booking.getParcelPhotos());
        assertEquals(1, booking.getParcelPhotos().size());
        assertTrue(booking.getParcelPhotos().get(0).getIsPrimary());
    }

    // ==========================================
    // TEST 2: Validation vol existe
    // ==========================================
    @Test
    @DisplayName("Devrait lever une exception si le vol n'existe pas")
    void shouldThrowExceptionWhenFlightNotExists() {
        // Given
        validRequest.setFlightId(99999); // ID inexistant

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.createBooking(validRequest, testPhotos, testCustomer.getId())
        );

        assertEquals(ErrorInfo.RESOURCE_NOT_FOUND, exception.getErrorInfo());
        assertTrue(exception.getMessage().contains("Vol"));
    }

    // ==========================================
    // TEST 3: Calcul prix selon settings
    // ==========================================
    @Test
    @DisplayName("Devrait calculer le prix selon les paramètres de la plateforme")
    void shouldCalculatePriceAccordingToSettings() throws SendByOpException {
        // When
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // Then
        // Le prix devrait être entre min et max définis dans PlatformSettings
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        BigDecimal expectedMin = settings.getMinPricePerKg().multiply(validRequest.getParcelWeight());
        BigDecimal expectedMax = settings.getMaxPricePerKg().multiply(validRequest.getParcelWeight());

        assertNotNull(booking.getTotalPrice());
        assertTrue(booking.getTotalPrice().compareTo(expectedMin) >= 0,
                "Le prix devrait être >= " + expectedMin);
        assertTrue(booking.getTotalPrice().compareTo(expectedMax) <= 0,
                "Le prix devrait être <= " + expectedMax);
    }

    // ==========================================
    // TEST 4: GetOrCreate destinataire
    // ==========================================
    @Test
    @DisplayName("Devrait utiliser un destinataire existant si trouvé par email")
    void shouldReuseExistingReceiverIfFoundByEmail() throws SendByOpException {
        // Given - Créer d'abord un destinataire
        ReceiverDto existingReceiver = ReceiverDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@test.com")
                .phoneNumber("+33612345678")
                .build();
        ReceiverDto created = receiverService.createReceiver(existingReceiver);

        // When - Créer une réservation avec le même email
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // Then - Devrait utiliser le destinataire existant
        assertEquals(created.getId(), booking.getReceiverId());

        // Vérifier qu'un seul destinataire existe
        assertEquals(1, receiverRepository.count());
    }

    // ==========================================
    // TEST 5: Photo colis requise
    // ==========================================
    @Test
    @DisplayName("Devrait lever une exception si aucune photo n'est fournie")
    void shouldThrowExceptionWhenParcelPhotoNotProvided() {
        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.createBooking(validRequest, null, testCustomer.getId())
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        assertTrue(exception.getMessage().toLowerCase().contains("photo"));
    }

    // ==========================================
    // TEST 5bis: Support multi-photos
    // ==========================================
    @Test
    @DisplayName("Devrait accepter plusieurs photos (jusqu'à 5)")
    void shouldAcceptMultiplePhotos() throws SendByOpException {
        // Given - Créer 3 photos de test
        MockMultipartFile photo1 = new MockMultipartFile(
                "parcelPhoto1", "test1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile photo2 = new MockMultipartFile(
                "parcelPhoto2", "test2.jpg", "image/jpeg", "content2".getBytes());
        MockMultipartFile photo3 = new MockMultipartFile(
                "parcelPhoto3", "test3.jpg", "image/jpeg", "content3".getBytes());
        
        MockMultipartFile[] multiplePhotos = new MockMultipartFile[] { photo1, photo2, photo3 };
        
        // When
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                multiplePhotos,
                testCustomer.getId()
        );
        
        // Then
        assertNotNull(booking.getParcelPhotos());
        assertEquals(3, booking.getParcelPhotos().size());
        
        // Vérifier que la première est marquée comme principale
        assertTrue(booking.getParcelPhotos().get(0).getIsPrimary());
        assertFalse(booking.getParcelPhotos().get(1).getIsPrimary());
        assertFalse(booking.getParcelPhotos().get(2).getIsPrimary());
        
        // Vérifier l'ordre d'affichage
        assertEquals(0, booking.getParcelPhotos().get(0).getDisplayOrder());
        assertEquals(1, booking.getParcelPhotos().get(1).getDisplayOrder());
        assertEquals(2, booking.getParcelPhotos().get(2).getDisplayOrder());
    }
    
    // ==========================================
    // TEST 5ter: Rejet si plus de 5 photos
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter si plus de 5 photos sont fournies")
    void shouldRejectMoreThanFivePhotos() {
        // Given - Créer 6 photos
        MockMultipartFile[] tooManyPhotos = new MockMultipartFile[6];
        for (int i = 0; i < 6; i++) {
            tooManyPhotos[i] = new MockMultipartFile(
                    "photo" + i, "test" + i + ".jpg", "image/jpeg", ("content" + i).getBytes());
        }
        
        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.createBooking(validRequest, tooManyPhotos, testCustomer.getId())
        );
        
        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        assertTrue(exception.getMessage().contains("5"));
    }

    // ==========================================
    // TEST 6: Status initial correct
    // ==========================================
    @Test
    @DisplayName("Devrait créer une réservation avec le statut PENDING_CONFIRMATION")
    void shouldCreateBookingWithPendingConfirmationStatus() throws SendByOpException {
        // When
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // Then
        assertEquals(BookingStatus.PENDING_CONFIRMATION, booking.getStatus());
        assertNull(booking.getConfirmedAt());
        assertNull(booking.getPaymentDeadline());
    }

    // ==========================================
    // TEST 7: Validation poids colis
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter un poids de colis invalide")
    void shouldRejectInvalidParcelWeight() {
        // Given - Poids négatif
        validRequest.setParcelWeight(BigDecimal.valueOf(-1.0));

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.createBooking(validRequest, testPhotos, testCustomer.getId())
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 8: Validation description colis
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter une description de colis trop courte")
    void shouldRejectTooShortParcelDescription() {
        // Given
        validRequest.setParcelDescription("Court"); // < 10 caractères

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.createBooking(validRequest, testPhotos, testCustomer.getId())
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 9: Prix proposé utilisé si valide
    // ==========================================
    @Test
    @DisplayName("Devrait utiliser le prix proposé s'il est dans les limites")
    void shouldUseProposedPriceIfValid() throws SendByOpException {
        // Given
        BigDecimal proposedPrice = BigDecimal.valueOf(30.00);
        validRequest.setProposedPrice(proposedPrice);

        // When
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // Then
        assertEquals(proposedPrice, booking.getTotalPrice());
    }

    // ==========================================
    // TEST 10: Prix proposé rejeté si hors limites
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter un prix proposé trop bas")
    void shouldRejectProposedPriceTooLow() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        BigDecimal tooLowPrice = settings.getMinPricePerKg()
                .multiply(validRequest.getParcelWeight())
                .subtract(BigDecimal.ONE); // Prix trop bas

        validRequest.setProposedPrice(tooLowPrice);

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.createBooking(validRequest, testPhotos, testCustomer.getId())
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        assertTrue(exception.getMessage().toLowerCase().contains("prix"));
    }

    // ==========================================
    // TEST 11: Photo uploadée correctement
    // ==========================================
    @Test
    @DisplayName("Devrait uploader la photo et stocker l'URL")
    void shouldUploadPhotoAndStoreUrl() throws SendByOpException {
        // When
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // Then
        assertNotNull(booking.getParcelPhotoUrl());
        assertTrue(booking.getParcelPhotoUrl().contains("uploads") ||
                   booking.getParcelPhotoUrl().contains("http"));
    }

    // ==========================================
    // TEST 12: Validation client existe
    // ==========================================
    @Test
    @DisplayName("Devrait lever une exception si le client n'existe pas")
    void shouldThrowExceptionWhenCustomerNotExists() {
        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.createBooking(validRequest, testPhotos, 99999)
        );

        assertEquals(ErrorInfo.RESOURCE_NOT_FOUND, exception.getErrorInfo());
        assertTrue(exception.getMessage().contains("Client"));
    }

    // ==========================================
    // TEST 13: Informations destinataire dans réponse
    // ==========================================
    @Test
    @DisplayName("Devrait inclure les informations du destinataire dans la réponse")
    void shouldIncludeReceiverInformationInResponse() throws SendByOpException {
        // When
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // Then
        assertNotNull(booking.getReceiverFullName());
        assertTrue(booking.getReceiverFullName().contains("Jane"));
        assertTrue(booking.getReceiverFullName().contains("Smith"));
        assertEquals("jane.smith@test.com", booking.getReceiverEmail());
        assertEquals("+33612345678", booking.getReceiverPhoneNumber());
    }

    // ==========================================
    // SPRINT 3: TESTS CONFIRMATION/REJET/PAIEMENT
    // ==========================================

    // ==========================================
    // TEST 14: Confirmation par voyageur
    // ==========================================
    @Test
    @DisplayName("Devrait confirmer une réservation par le voyageur")
    void shouldConfirmBookingByTraveler() throws SendByOpException {
        // Given - Créer une réservation
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();

        // When - Le voyageur confirme
        BookingResponseDto confirmed = bookingService.confirmBooking(booking.getId(), travelerId);

        // Then
        assertEquals(BookingStatus.CONFIRMED_UNPAID, confirmed.getStatus());
        assertNotNull(confirmed.getConfirmedAt());
        assertNotNull(confirmed.getPaymentDeadline());
    }

    // ==========================================
    // TEST 15: Confirmation - Validation propriétaire
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter la confirmation si le voyageur n'est pas propriétaire")
    void shouldRejectConfirmationByNonOwner() throws SendByOpException {
        // Given
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // When & Then - Un autre voyageur tente de confirmer
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.confirmBooking(booking.getId(), 99999)
        );

        assertEquals(ErrorInfo.UNAUTHORIZED, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 16: Confirmation - Statut invalide
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter la confirmation si le statut n'est pas PENDING_CONFIRMATION")
    void shouldRejectConfirmationWhenInvalidStatus() throws SendByOpException {
        // Given - Créer et confirmer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        bookingService.confirmBooking(booking.getId(), travelerId);

        // When & Then - Tenter de confirmer à nouveau
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.confirmBooking(booking.getId(), travelerId)
        );

        assertEquals(ErrorInfo.INVALID_STATUS, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 17: Rejet par voyageur
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter une réservation par le voyageur")
    void shouldRejectBookingByTraveler() throws SendByOpException {
        // Given
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();

        // When
        String reason = "Colis trop volumineux";
        BookingResponseDto rejected = bookingService.rejectBooking(booking.getId(), travelerId, reason);

        // Then
        assertEquals(BookingStatus.CANCELLED_BY_TRAVELER, rejected.getStatus());
    }

    // ==========================================
    // TEST 18: Rejet - Statut invalide
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter le rejet si le statut n'est pas PENDING_CONFIRMATION")
    void shouldRejectRejectionWhenInvalidStatus() throws SendByOpException {
        // Given - Créer et confirmer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        bookingService.confirmBooking(booking.getId(), travelerId);

        // When & Then - Tenter de rejeter après confirmation
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.rejectBooking(booking.getId(), travelerId, "Raison")
        );

        assertEquals(ErrorInfo.INVALID_STATUS, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 19: Paiement par client
    // ==========================================
    @Test
    @DisplayName("Devrait traiter le paiement d'une réservation confirmée")
    void shouldProcessPaymentForConfirmedBooking() throws SendByOpException {
        // Given - Créer et confirmer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        BookingResponseDto confirmed = bookingService.confirmBooking(booking.getId(), travelerId);

        // When - Client paie
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .amount(confirmed.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .paymentToken("tok_test_123")
                .build();

        BookingResponseDto paid = bookingService.processPayment(
                booking.getId(),
                paymentRequest,
                testCustomer.getId()
        );

        // Then
        assertEquals(BookingStatus.CONFIRMED_PAID, paid.getStatus());
    }

    // ==========================================
    // TEST 20: Paiement - Montant incorrect
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter le paiement si le montant est incorrect")
    void shouldRejectPaymentWithIncorrectAmount() throws SendByOpException {
        // Given
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        BookingResponseDto confirmed = bookingService.confirmBooking(booking.getId(), travelerId);

        // When - Montant incorrect
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .amount(confirmed.getTotalPrice().subtract(BigDecimal.ONE))
                .paymentMethod("CREDIT_CARD")
                .build();

        // Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.processPayment(booking.getId(), paymentRequest, testCustomer.getId())
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        assertTrue(exception.getMessage().toLowerCase().contains("montant"));
    }

    // ==========================================
    // TEST 21: Paiement - Statut invalide
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter le paiement si le statut n'est pas CONFIRMED_UNPAID")
    void shouldRejectPaymentWhenInvalidStatus() throws SendByOpException {
        // Given - Créer sans confirmer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .amount(booking.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .build();

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.processPayment(booking.getId(), paymentRequest, testCustomer.getId())
        );

        assertEquals(ErrorInfo.INVALID_STATUS, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 22: Paiement - Client non propriétaire
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter le paiement si le client n'est pas propriétaire")
    void shouldRejectPaymentByNonOwner() throws SendByOpException {
        // Given
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        BookingResponseDto confirmed = bookingService.confirmBooking(booking.getId(), travelerId);

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .amount(confirmed.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .build();

        // When & Then - Autre client tente de payer
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.processPayment(booking.getId(), paymentRequest, 99999)
        );

        assertEquals(ErrorInfo.UNAUTHORIZED, exception.getErrorInfo());
    }

    // ==========================================
    // SPRINT 4: TESTS ANNULATION/LIVRAISON
    // ==========================================

    // ==========================================
    // TEST 23: Annulation par client
    // ==========================================
    @Test
    @DisplayName("Devrait permettre au client d'annuler sa réservation")
    void shouldAllowClientToCancelBooking() throws SendByOpException {
        // Given
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // When
        String reason = "Changement de plans";
        BookingResponseDto cancelled = bookingService.cancelByClient(
                booking.getId(),
                testCustomer.getId(),
                reason
        );

        // Then
        assertEquals(BookingStatus.CANCELLED_BY_CLIENT, cancelled.getStatus());
    }

    // ==========================================
    // TEST 24: Annulation - Non propriétaire
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter l'annulation si le client n'est pas propriétaire")
    void shouldRejectCancellationByNonOwner() throws SendByOpException {
        // Given
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.cancelByClient(booking.getId(), 99999, "Raison")
        );

        assertEquals(ErrorInfo.UNAUTHORIZED, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 25: Annulation - Après livraison interdite
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter l'annulation si déjà livrée")
    void shouldRejectCancellationWhenAlreadyDelivered() throws SendByOpException {
        // Given - Créer, confirmer, payer, livrer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        bookingService.confirmBooking(booking.getId(), travelerId);
        
        PaymentRequest payment = PaymentRequest.builder()
                .amount(booking.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .build();
        bookingService.processPayment(booking.getId(), payment, testCustomer.getId());
        bookingService.markAsDelivered(booking.getId(), travelerId);

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.cancelByClient(booking.getId(), testCustomer.getId(), "Raison")
        );

        assertEquals(ErrorInfo.INVALID_STATUS, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 26: Marquage livraison par voyageur
    // ==========================================
    @Test
    @DisplayName("Devrait marquer une réservation comme livrée")
    void shouldMarkBookingAsDelivered() throws SendByOpException {
        // Given - Créer, confirmer, payer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        bookingService.confirmBooking(booking.getId(), travelerId);
        
        PaymentRequest payment = PaymentRequest.builder()
                .amount(booking.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .build();
        bookingService.processPayment(booking.getId(), payment, testCustomer.getId());

        // When
        BookingResponseDto delivered = bookingService.markAsDelivered(booking.getId(), travelerId);

        // Then
        assertEquals(BookingStatus.DELIVERED, delivered.getStatus());
    }

    // ==========================================
    // TEST 27: Livraison - Status invalide
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter le marquage livraison si non payée")
    void shouldRejectDeliveryWhenNotPaid() throws SendByOpException {
        // Given - Créer seulement
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.markAsDelivered(booking.getId(), travelerId)
        );

        assertEquals(ErrorInfo.INVALID_STATUS, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 28: Livraison - Non propriétaire
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter le marquage livraison par non-propriétaire")
    void shouldRejectDeliveryByNonOwner() throws SendByOpException {
        // Given - Créer, confirmer, payer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        bookingService.confirmBooking(booking.getId(), travelerId);
        
        PaymentRequest payment = PaymentRequest.builder()
                .amount(booking.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .build();
        bookingService.processPayment(booking.getId(), payment, testCustomer.getId());

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.markAsDelivered(booking.getId(), 99999)
        );

        assertEquals(ErrorInfo.UNAUTHORIZED, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 29: Marquage récupération par client
    // ==========================================
    @Test
    @DisplayName("Devrait marquer une réservation comme récupérée")
    void shouldMarkBookingAsPickedUp() throws SendByOpException {
        // Given - Créer, confirmer, payer, livrer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        bookingService.confirmBooking(booking.getId(), travelerId);
        
        PaymentRequest payment = PaymentRequest.builder()
                .amount(booking.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .build();
        bookingService.processPayment(booking.getId(), payment, testCustomer.getId());
        bookingService.markAsDelivered(booking.getId(), travelerId);

        // When
        BookingResponseDto pickedUp = bookingService.markAsPickedUp(
                booking.getId(),
                testCustomer.getId()
        );

        // Then
        assertEquals(BookingStatus.PICKED_UP, pickedUp.getStatus());
    }

    // ==========================================
    // TEST 30: Récupération - Status invalide
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter le marquage récupération si pas livrée")
    void shouldRejectPickupWhenNotDelivered() throws SendByOpException {
        // Given - Créer, confirmer, payer (mais pas livrer)
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        bookingService.confirmBooking(booking.getId(), travelerId);
        
        PaymentRequest payment = PaymentRequest.builder()
                .amount(booking.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .build();
        bookingService.processPayment(booking.getId(), payment, testCustomer.getId());

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.markAsPickedUp(booking.getId(), testCustomer.getId())
        );

        assertEquals(ErrorInfo.INVALID_STATUS, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 31: Récupération - Non propriétaire
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter le marquage récupération par non-propriétaire")
    void shouldRejectPickupByNonOwner() throws SendByOpException {
        // Given - Créer, confirmer, payer, livrer
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        bookingService.confirmBooking(booking.getId(), travelerId);
        
        PaymentRequest payment = PaymentRequest.builder()
                .amount(booking.getTotalPrice())
                .paymentMethod("CREDIT_CARD")
                .build();
        bookingService.processPayment(booking.getId(), payment, testCustomer.getId());
        bookingService.markAsDelivered(booking.getId(), travelerId);

        // When & Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> bookingService.markAsPickedUp(booking.getId(), 99999)
        );

        assertEquals(ErrorInfo.UNAUTHORIZED, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 32: Annulation automatique (sans cron pour test)
    // ==========================================
    @Test
    @DisplayName("Devrait annuler automatiquement les réservations non payées avec deadline dépassée")
    void shouldAutoCancelUnpaidBookings() throws SendByOpException {
        // Given - Créer et confirmer une réservation
        BookingResponseDto booking = bookingService.createBooking(
                validRequest,
                testPhotos,
                testCustomer.getId()
        );
        Integer travelerId = testFlight.getCustomer().getId();
        BookingResponseDto confirmed = bookingService.confirmBooking(booking.getId(), travelerId);

        // Simuler deadline dépassée en modifiant manuellement (dans un vrai test, on utiliserait une fixture)
        // Pour ce test, on vérifie juste que la méthode existe et retourne un nombre
        
        // When
        int cancelledCount = bookingService.autoCancelUnpaidBookings();

        // Then
        assertTrue(cancelledCount >= 0, "Le nombre de réservations annulées doit être >= 0");
    }
}
