package com.sendByOP.expedition.services;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PlatformSettingsDto;
import com.sendByOP.expedition.models.entities.PlatformSettings;
import com.sendByOP.expedition.repositories.PlatformSettingsRepository;
import com.sendByOP.expedition.services.impl.PlatformSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PlatformSettingsService
 * Approche TDD: Tests écrits AVANT l'implémentation
 */
@SpringBootTest
@Transactional
@DisplayName("PlatformSettingsService Tests")
class PlatformSettingsServiceTest {

    @Autowired
    private PlatformSettingsService platformSettingsService;

    @Autowired
    private PlatformSettingsRepository platformSettingsRepository;

    @BeforeEach
    void setUp() {
        // Nettoyer la table
        platformSettingsRepository.deleteAll();
    }

    // ==========================================
    // TEST 1: Récupération des settings par défaut
    // ==========================================
    @Test
    @DisplayName("Devrait retourner les paramètres par défaut si aucun n'existe")
    void shouldReturnDefaultSettingsWhenNoneExist() throws SendByOpException {
        // When
        PlatformSettingsDto settings = platformSettingsService.getSettings();

        // Then
        assertNotNull(settings);
        assertEquals(BigDecimal.valueOf(5.00), settings.getMinPricePerKg());
        assertEquals(BigDecimal.valueOf(50.00), settings.getMaxPricePerKg());
        assertEquals(BigDecimal.valueOf(70.00), settings.getTravelerPercentage());
        assertEquals(BigDecimal.valueOf(25.00), settings.getPlatformPercentage());
        assertEquals(BigDecimal.valueOf(5.00), settings.getVatPercentage());
        assertEquals(12, settings.getPaymentTimeoutHours());
        assertEquals(24, settings.getAutoPayoutDelayHours());
        assertEquals(24, settings.getCancellationDeadlineHours());
        assertEquals(BigDecimal.valueOf(0.50), settings.getLateCancellationPenalty());
    }

    // ==========================================
    // TEST 2: Mise à jour des settings
    // ==========================================
    @Test
    @DisplayName("Devrait mettre à jour les paramètres existants")
    void shouldUpdateExistingSettings() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        // When - Modifier les tarifs
        settings.setMinPricePerKg(BigDecimal.valueOf(10.00));
        settings.setMaxPricePerKg(BigDecimal.valueOf(100.00));
        PlatformSettingsDto updated = platformSettingsService.updateSettings(settings);

        // Then
        assertNotNull(updated);
        assertEquals(BigDecimal.valueOf(10.00), updated.getMinPricePerKg());
        assertEquals(BigDecimal.valueOf(100.00), updated.getMaxPricePerKg());
        assertNotNull(updated.getUpdatedAt());
    }

    // ==========================================
    // TEST 3: Validation somme pourcentages
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter si la somme des pourcentages n'est pas égale à 100%")
    void shouldRejectIfPercentageSumIsNot100() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        // When - Modifier pour que la somme != 100
        settings.setTravelerPercentage(BigDecimal.valueOf(60.00));
        settings.setPlatformPercentage(BigDecimal.valueOf(30.00));
        settings.setVatPercentage(BigDecimal.valueOf(5.00)); // Total = 95%

        // Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> platformSettingsService.updateSettings(settings)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        assertTrue(exception.getMessage().contains("100"));
    }

    // ==========================================
    // TEST 4: Validation prix min < prix max
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter si prix minimum >= prix maximum")
    void shouldRejectIfMinPriceGreaterOrEqualMaxPrice() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        // When - Prix min >= prix max
        settings.setMinPricePerKg(BigDecimal.valueOf(50.00));
        settings.setMaxPricePerKg(BigDecimal.valueOf(40.00));

        // Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> platformSettingsService.updateSettings(settings)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        assertTrue(exception.getMessage().contains("inférieur") || 
                   exception.getMessage().contains("prix"));
    }

    // ==========================================
    // TEST 5: Validation délai paiement
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter si délai paiement < 2h ou > 24h")
    void shouldRejectInvalidPaymentTimeout() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        // When - Délai trop court
        settings.setPaymentTimeoutHours(1);

        // Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> platformSettingsService.updateSettings(settings)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        
        // When - Délai trop long
        settings.setPaymentTimeoutHours(25);

        // Then
        exception = assertThrows(
                SendByOpException.class,
                () -> platformSettingsService.updateSettings(settings)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 6: Validation délai versement
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter si délai versement < 12h ou > 72h")
    void shouldRejectInvalidPayoutDelay() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        // When - Délai trop court
        settings.setAutoPayoutDelayHours(10);

        // Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> platformSettingsService.updateSettings(settings)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        
        // When - Délai trop long
        settings.setAutoPayoutDelayHours(80);

        // Then
        exception = assertThrows(
                SendByOpException.class,
                () -> platformSettingsService.updateSettings(settings)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 7: Validation pénalité
    // ==========================================
    @Test
    @DisplayName("Devrait rejeter si pénalité < 0 ou > 1")
    void shouldRejectInvalidPenalty() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        // When - Pénalité négative
        settings.setLateCancellationPenalty(BigDecimal.valueOf(-0.1));

        // Then
        SendByOpException exception = assertThrows(
                SendByOpException.class,
                () -> platformSettingsService.updateSettings(settings)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
        
        // When - Pénalité > 100%
        settings.setLateCancellationPenalty(BigDecimal.valueOf(1.5));

        // Then
        exception = assertThrows(
                SendByOpException.class,
                () -> platformSettingsService.updateSettings(settings)
        );

        assertEquals(ErrorInfo.INVALID_DATA, exception.getErrorInfo());
    }

    // ==========================================
    // TEST 8: Singleton pattern
    // ==========================================
    @Test
    @DisplayName("Devrait avoir une seule ligne de paramètres en base")
    void shouldHaveOnlyOneSettingsRecord() throws SendByOpException {
        // When
        PlatformSettingsDto settings1 = platformSettingsService.getSettings();
        settings1.setMinPricePerKg(BigDecimal.valueOf(15.00));
        platformSettingsService.updateSettings(settings1);
        
        PlatformSettingsDto settings2 = platformSettingsService.getSettings();

        // Then
        assertEquals(settings1.getId(), settings2.getId());
        assertEquals(BigDecimal.valueOf(15.00), settings2.getMinPricePerKg());
        
        // Vérifier qu'une seule ligne existe
        long count = platformSettingsRepository.count();
        assertEquals(1, count);
    }

    // ==========================================
    // TEST 9: Mise à jour timestamp
    // ==========================================
    @Test
    @DisplayName("Devrait mettre à jour le timestamp lors de la modification")
    void shouldUpdateTimestampOnModification() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        // When
        settings.setMinPricePerKg(BigDecimal.valueOf(20.00));
        PlatformSettingsDto updated = platformSettingsService.updateSettings(settings);

        // Then
        assertNotNull(updated.getUpdatedAt());
    }

    // ==========================================
    // TEST 10: Valeurs valides acceptées
    // ==========================================
    @Test
    @DisplayName("Devrait accepter des valeurs valides")
    void shouldAcceptValidSettings() throws SendByOpException {
        // Given
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        
        // When - Modifier avec des valeurs valides
        settings.setMinPricePerKg(BigDecimal.valueOf(8.00));
        settings.setMaxPricePerKg(BigDecimal.valueOf(60.00));
        settings.setTravelerPercentage(BigDecimal.valueOf(65.00));
        settings.setPlatformPercentage(BigDecimal.valueOf(30.00));
        settings.setVatPercentage(BigDecimal.valueOf(5.00)); // Total = 100%
        settings.setPaymentTimeoutHours(10);
        settings.setAutoPayoutDelayHours(48);
        settings.setCancellationDeadlineHours(36);
        settings.setLateCancellationPenalty(BigDecimal.valueOf(0.30));
        
        PlatformSettingsDto updated = platformSettingsService.updateSettings(settings);

        // Then
        assertNotNull(updated);
        assertEquals(BigDecimal.valueOf(8.00), updated.getMinPricePerKg());
        assertEquals(BigDecimal.valueOf(60.00), updated.getMaxPricePerKg());
        assertEquals(BigDecimal.valueOf(65.00), updated.getTravelerPercentage());
        assertEquals(10, updated.getPaymentTimeoutHours());
        assertEquals(48, updated.getAutoPayoutDelayHours());
        assertEquals(36, updated.getCancellationDeadlineHours());
        assertEquals(BigDecimal.valueOf(0.30), updated.getLateCancellationPenalty());
    }
}
