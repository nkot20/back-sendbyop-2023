package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.ReceiverMapper;
import com.sendByOP.expedition.models.dto.ReceiverDto;
import com.sendByOP.expedition.models.entities.Receiver;
import com.sendByOP.expedition.models.enums.RecipientStatus;
import com.sendByOP.expedition.repositories.ReceiverRepository;
import com.sendByOP.expedition.services.iServices.IReceiverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service de gestion des destinataires de colis
 * Implémente le contrôle des doublons et la logique getOrCreate
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReceiverService implements IReceiverService {

    private final ReceiverRepository receiverRepository;
    private final ReceiverMapper receiverMapper;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Override
    public ReceiverDto createReceiver(ReceiverDto receiverDto) throws SendByOpException {
        log.debug("Creating new receiver: {}", receiverDto.getEmail());
        
        // Validation des données
        validateReceiverData(receiverDto);
        
        // Vérifier les doublons
        if (StringUtils.hasText(receiverDto.getEmail())) {
            if (receiverRepository.existsByEmail(receiverDto.getEmail())) {
                log.error("Duplicate email detected: {}", receiverDto.getEmail());
                throw new SendByOpException(ErrorInfo.DUPLICATE_ENTRY, 
                    "Un destinataire avec cet email existe déjà");
            }
        }
        
        if (StringUtils.hasText(receiverDto.getPhoneNumber())) {
            if (receiverRepository.existsByPhoneNumber(receiverDto.getPhoneNumber())) {
                log.error("Duplicate phone number detected: {}", receiverDto.getPhoneNumber());
                throw new SendByOpException(ErrorInfo.DUPLICATE_ENTRY, 
                    "Un destinataire avec ce numéro de téléphone existe déjà");
            }
        }
        
        // Créer l'entité
        Receiver receiver = receiverMapper.toEntity(receiverDto);
        receiver.setStatus(RecipientStatus.ACTIVE);
        // Les timestamps sont gérés automatiquement par @CreationTimestamp et @UpdateTimestamp
        
        // Sauvegarder
        Receiver saved = receiverRepository.save(receiver);
        log.info("Receiver created successfully with ID: {}", saved.getId());
        
        return receiverMapper.toDto(saved);
    }

    @Override
    public ReceiverDto getReceiverByEmail(String email) throws SendByOpException {
        log.debug("Getting receiver by email: {}", email);
        
        if (!StringUtils.hasText(email)) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, "L'email est requis");
        }
        
        Receiver receiver = receiverRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("Receiver not found with email: {}", email);
                return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                    "Destinataire non trouvé avec cet email");
            });
        
        return receiverMapper.toDto(receiver);
    }

    @Override
    public ReceiverDto getReceiverByPhoneNumber(String phoneNumber) throws SendByOpException {
        log.debug("Getting receiver by phone number: {}", phoneNumber);
        
        if (!StringUtils.hasText(phoneNumber)) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Le numéro de téléphone est requis");
        }
        
        Receiver receiver = receiverRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> {
                log.error("Receiver not found with phone number: {}", phoneNumber);
                return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                    "Destinataire non trouvé avec ce numéro");
            });
        
        return receiverMapper.toDto(receiver);
    }

    @Override
    public ReceiverDto getOrCreateReceiver(ReceiverDto receiverDto) throws SendByOpException {
        log.debug("Getting or creating receiver: email={}, phone={}", 
            receiverDto.getEmail(), receiverDto.getPhoneNumber());
        
        // Validation: au moins email OU téléphone requis
        if (!StringUtils.hasText(receiverDto.getEmail()) && 
            !StringUtils.hasText(receiverDto.getPhoneNumber())) {
            log.error("Both email and phone number are null");
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Email ou téléphone requis");
        }
        
        // Chercher par email en priorité
        if (StringUtils.hasText(receiverDto.getEmail())) {
            Optional<Receiver> existingByEmail = 
                receiverRepository.findByEmail(receiverDto.getEmail());
            
            if (existingByEmail.isPresent()) {
                log.debug("Receiver found by email");
                return receiverMapper.toDto(existingByEmail.get());
            }
        }
        
        // Chercher par téléphone
        if (StringUtils.hasText(receiverDto.getPhoneNumber())) {
            Optional<Receiver> existingByPhone = 
                receiverRepository.findByPhoneNumber(receiverDto.getPhoneNumber());
            
            if (existingByPhone.isPresent()) {
                log.debug("Receiver found by phone number");
                return receiverMapper.toDto(existingByPhone.get());
            }
        }
        
        // Aucun trouvé, créer nouveau
        log.debug("Receiver not found, creating new one");
        return createReceiver(receiverDto);
    }

    @Override
    public ReceiverDto updateReceiver(ReceiverDto receiverDto) throws SendByOpException {
        log.debug("Updating receiver with ID: {}", receiverDto.getId());
        
        if (receiverDto.getId() == null) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "L'ID du destinataire est requis pour la mise à jour");
        }
        
        // Vérifier que le destinataire existe
        Receiver existing = receiverRepository.findById(receiverDto.getId())
            .orElseThrow(() -> {
                log.error("Receiver not found with ID: {}", receiverDto.getId());
                return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                    "Destinataire non trouvé");
            });
        
        // Mettre à jour les champs
        if (StringUtils.hasText(receiverDto.getFirstName())) {
            existing.setFirstName(receiverDto.getFirstName());
        }
        if (StringUtils.hasText(receiverDto.getLastName())) {
            existing.setLastName(receiverDto.getLastName());
        }
        if (StringUtils.hasText(receiverDto.getAddress())) {
            existing.setAddress(receiverDto.getAddress());
        }
        if (StringUtils.hasText(receiverDto.getCity())) {
            existing.setCity(receiverDto.getCity());
        }
        if (StringUtils.hasText(receiverDto.getCountry())) {
            existing.setCountry(receiverDto.getCountry());
        }
        if (receiverDto.getStatus() != null) {
            existing.setStatus(receiverDto.getStatus());
        }
        // updatedAt est géré automatiquement par @UpdateTimestamp
        
        Receiver updated = receiverRepository.save(existing);
        log.info("Receiver updated successfully: {}", updated.getId());
        
        return receiverMapper.toDto(updated);
    }

    @Override
    public boolean receiverExists(String email, String phoneNumber) {
        log.debug("Checking if receiver exists: email={}, phone={}", email, phoneNumber);
        
        if (StringUtils.hasText(email)) {
            boolean existsByEmail = receiverRepository.existsByEmail(email);
            if (existsByEmail) {
                return true;
            }
        }
        
        if (StringUtils.hasText(phoneNumber)) {
            boolean existsByPhone = receiverRepository.existsByPhoneNumber(phoneNumber);
            if (existsByPhone) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Valide les données du destinataire
     */
    private void validateReceiverData(ReceiverDto receiverDto) throws SendByOpException {
        // Vérifier les champs obligatoires
        if (!StringUtils.hasText(receiverDto.getFirstName())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, "Le prénom est requis");
        }
        
        if (!StringUtils.hasText(receiverDto.getLastName())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, "Le nom est requis");
        }
        
        // Au moins email ou téléphone requis
        if (!StringUtils.hasText(receiverDto.getEmail()) && 
            !StringUtils.hasText(receiverDto.getPhoneNumber())) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                "Email ou téléphone requis");
        }
        
        // Valider format email si fourni
        if (StringUtils.hasText(receiverDto.getEmail())) {
            if (!EMAIL_PATTERN.matcher(receiverDto.getEmail()).matches()) {
                throw new SendByOpException(ErrorInfo.INVALID_DATA, 
                    "Format d'email invalide");
            }
        }
    }
}
