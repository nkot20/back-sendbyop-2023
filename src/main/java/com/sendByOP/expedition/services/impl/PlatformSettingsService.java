package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.PlatformSettingsMapper;
import com.sendByOP.expedition.models.dto.PlatformSettingsDto;
import com.sendByOP.expedition.models.entities.PlatformSettings;
import com.sendByOP.expedition.repositories.PlatformSettingsRepository;
import com.sendByOP.expedition.services.iServices.IPlatformSettingsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service de gestion des paramètres de la plateforme
 * Pattern Singleton: Une seule ligne de configuration existe
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlatformSettingsService implements IPlatformSettingsService {

    private final PlatformSettingsRepository platformSettingsRepository;
    private final PlatformSettingsMapper platformSettingsMapper;

    @Override
    public PlatformSettingsDto getSettings() throws SendByOpException {
        log.debug("Getting platform settings");
        
        PlatformSettings settings = platformSettingsRepository.getSettings();
        
        // Si aucun paramètre n'existe, créer les valeurs par défaut
        if (settings == null) {
            log.info("No settings found, creating default settings");
            settings = createDefaultSettings();
        }
        
        return platformSettingsMapper.toDto(settings);
    }

    @Override
    public PlatformSettingsDto updateSettings(PlatformSettingsDto settingsDto) throws SendByOpException {
        log.debug("Updating platform settings");
        
        // Validation des données
        validateSettings(settingsDto);
        
        // Récupérer ou créer les settings
        PlatformSettings settings = platformSettingsRepository.getSettings();
        if (settings == null) {
            log.info("No settings found, creating new");
            settings = new PlatformSettings();
        }
        
        // Mettre à jour les champs
        settings.setMinPricePerKg(settingsDto.getMinPricePerKg());
        settings.setMaxPricePerKg(settingsDto.getMaxPricePerKg());
        settings.setTravelerPercentage(settingsDto.getTravelerPercentage());
        settings.setPlatformPercentage(settingsDto.getPlatformPercentage());
        settings.setVatPercentage(settingsDto.getVatPercentage());
        settings.setPaymentTimeoutHours(settingsDto.getPaymentTimeoutHours());
        settings.setAutoPayoutDelayHours(settingsDto.getAutoPayoutDelayHours());
        settings.setCancellationDeadlineHours(settingsDto.getCancellationDeadlineHours());
        settings.setLateCancellationPenalty(settingsDto.getLateCancellationPenalty());
        settings.setUpdatedAt(LocalDateTime.now());
        
        // Sauvegarder
        PlatformSettings saved = platformSettingsRepository.save(settings);
        log.info("Platform settings updated successfully");
        
        return platformSettingsMapper.toDto(saved);
    }
    
    /**
     * Crée les paramètres par défaut
     */
    private PlatformSettings createDefaultSettings() {
        PlatformSettings settings = new PlatformSettings();
        
        // Tarifs
        settings.setMinPricePerKg(BigDecimal.valueOf(5.00));
        settings.setMaxPricePerKg(BigDecimal.valueOf(50.00));
        
        // Répartition
        settings.setTravelerPercentage(BigDecimal.valueOf(70.00));
        settings.setPlatformPercentage(BigDecimal.valueOf(25.00));
        settings.setVatPercentage(BigDecimal.valueOf(5.00));
        
        // Délais
        settings.setPaymentTimeoutHours(12);
        settings.setAutoPayoutDelayHours(24);
        settings.setCancellationDeadlineHours(24);
        
        // Pénalité
        settings.setLateCancellationPenalty(BigDecimal.valueOf(0.50));
        
        // Audit
        settings.setUpdatedAt(LocalDateTime.now());
        
        return platformSettingsRepository.save(settings);
    }
    
    /**
     * Valide les paramètres avant mise à jour
     */
    private void validateSettings(PlatformSettingsDto settings) throws SendByOpException {
        log.debug("Validating platform settings");
        
        // Validation 1: Somme des pourcentages = 100%
        BigDecimal percentageSum = settings.getTravelerPercentage()
                .add(settings.getPlatformPercentage())
                .add(settings.getVatPercentage());
        
        if (percentageSum.compareTo(BigDecimal.valueOf(100)) != 0) {
            log.error("Invalid percentage sum: {}", percentageSum);
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "La somme des pourcentages doit être égale à 100%");
        }
        
        // Validation 2: Prix minimum < prix maximum
        if (settings.getMinPricePerKg().compareTo(settings.getMaxPricePerKg()) >= 0) {
            log.error("Min price >= max price: {} >= {}",
                    settings.getMinPricePerKg(), settings.getMaxPricePerKg());
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Le prix minimum doit être inférieur au prix maximum");
        }
        
        // Validation 3: Délai paiement (2-24h)
        if (settings.getPaymentTimeoutHours() < 2 || settings.getPaymentTimeoutHours() > 24) {
            log.error("Invalid payment timeout: {}", settings.getPaymentTimeoutHours());
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Le délai de paiement doit être entre 2 et 24 heures");
        }
        
        // Validation 4: Délai versement (12-72h)
        if (settings.getAutoPayoutDelayHours() < 12 || settings.getAutoPayoutDelayHours() > 72) {
            log.error("Invalid payout delay: {}", settings.getAutoPayoutDelayHours());
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Le délai de versement doit être entre 12 et 72 heures");
        }
        
        // Validation 5: Délai annulation (12-72h)
        if (settings.getCancellationDeadlineHours() < 12 || settings.getCancellationDeadlineHours() > 72) {
            log.error("Invalid cancellation deadline: {}", settings.getCancellationDeadlineHours());
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Le délai d'annulation doit être entre 12 et 72 heures");
        }
        
        // Validation 6: Pénalité (0-1)
        if (settings.getLateCancellationPenalty().compareTo(BigDecimal.ZERO) < 0 ||
                settings.getLateCancellationPenalty().compareTo(BigDecimal.ONE) > 0) {
            log.error("Invalid penalty: {}", settings.getLateCancellationPenalty());
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "La pénalité doit être entre 0 et 1 (0% à 100%)");
        }
        
        log.debug("Settings validation passed");
    }
}
