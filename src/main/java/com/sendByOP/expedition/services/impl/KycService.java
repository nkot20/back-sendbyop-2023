package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.KycDocumentMapper;
import com.sendByOP.expedition.models.dto.KycDocumentDto;
import com.sendByOP.expedition.models.dto.KycSubmissionRequest;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.KycDocument;
import com.sendByOP.expedition.models.enums.KycStatus;
import com.sendByOP.expedition.repositories.CustomerRepository;
import com.sendByOP.expedition.repositories.KycDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de gestion KYC (Know Your Customer)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final CustomerRepository customerRepository;
    private final KycDocumentMapper kycDocumentMapper;
    private final SendMailService emailService;

    @Value("${kyc.upload.directory:uploads/kyc}")
    private String uploadDirectory;

    @Value("${kyc.max.file.size:5242880}") // 5MB par défaut
    private long maxFileSize;

    /**
     * Soumet un nouveau document KYC
     */
    public KycDocumentDto submitKycDocument(
            Integer customerId,
            KycSubmissionRequest request,
            MultipartFile frontImage,
            MultipartFile backImage) throws SendByOpException {

        log.info("Submitting KYC document for customer {}", customerId);

        // Validation du client
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, "Client non trouvé"));

        // Validation des fichiers
        validateFile(frontImage, "Image recto");
        if (backImage != null && !backImage.isEmpty()) {
            validateFile(backImage, "Image verso");
        }

        // Créer le document KYC
        KycDocument document = KycDocument.builder()
                .customer(customer)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .expiryDate(request.getExpiryDate())
                .countryOfIssue(request.getCountryOfIssue())
                .status(KycStatus.PENDING_REVIEW)
                .submittedAt(new Date())
                .build();

        // Sauvegarder les images
        try {
            String frontPath = saveFile(frontImage, customerId, "front");
            document.setFrontImagePath(frontPath);

            if (backImage != null && !backImage.isEmpty()) {
                String backPath = saveFile(backImage, customerId, "back");
                document.setBackImagePath(backPath);
            }
        } catch (IOException e) {
            log.error("Error saving KYC files for customer {}", customerId, e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Erreur lors de l'enregistrement des fichiers");
        }

        // Sauvegarder en base
        KycDocument saved = kycDocumentRepository.save(document);

        // Mettre à jour le statut du client
        customer.setIdentityUploaded(1);
        customer.setIdentityVerified(0); // En attente de validation
        customerRepository.save(customer);

        // Notification admin
        notifyAdminNewSubmission(customer, saved);

        log.info("KYC document submitted successfully for customer {}", customerId);
        return kycDocumentMapper.toDto(saved);
    }

    /**
     * Approuve un document KYC
     */
    public KycDocumentDto approveKycDocument(Integer documentId, String adminEmail) throws SendByOpException {
        log.info("Approving KYC document {} by admin {}", documentId, adminEmail);

        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, "Document KYC non trouvé"));

        if (document.getStatus() == KycStatus.APPROVED) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, "Ce document est déjà approuvé");
        }

        // Mettre à jour le document
        document.setStatus(KycStatus.APPROVED);
        document.setReviewedAt(new Date());
        document.setReviewedBy(adminEmail);
        document.setRejectionReason(null);

        KycDocument saved = kycDocumentRepository.save(document);

        // Mettre à jour le client
        Customer customer = document.getCustomer();
        customer.setIdentityVerified(1);
        customerRepository.save(customer);

        // Notification client
        notifyCustomerApproved(customer);

        log.info("KYC document {} approved successfully", documentId);
        return kycDocumentMapper.toDto(saved);
    }

    /**
     * Rejette un document KYC
     */
    public KycDocumentDto rejectKycDocument(Integer documentId, String reason, String adminEmail) throws SendByOpException {
        log.info("Rejecting KYC document {} by admin {}", documentId, adminEmail);

        if (reason == null || reason.trim().isEmpty()) {
            throw new SendByOpException(ErrorInfo.VALIDATION_ERROR, "Le motif de rejet est requis");
        }

        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, "Document KYC non trouvé"));

        // Mettre à jour le document
        document.setStatus(KycStatus.REJECTED);
        document.setReviewedAt(new Date());
        document.setReviewedBy(adminEmail);
        document.setRejectionReason(reason);

        KycDocument saved = kycDocumentRepository.save(document);

        // Mettre à jour le client
        Customer customer = document.getCustomer();
        customer.setIdentityVerified(0);
        customerRepository.save(customer);

        // Notification client
        notifyCustomerRejected(customer, reason);

        log.info("KYC document {} rejected", documentId);
        return kycDocumentMapper.toDto(saved);
    }

    /**
     * Récupère le statut KYC d'un client
     */
    public KycDocumentDto getCustomerKycStatus(Integer customerId) throws SendByOpException {
        return kycDocumentRepository.findFirstByCustomerIdOrderBySubmittedAtDesc(customerId)
                .map(kycDocumentMapper::toDto)
                .orElse(null);
    }

    /**
     * Récupère tous les documents KYC en attente
     */
    public Page<KycDocumentDto> getPendingDocuments(Pageable pageable) {
        return kycDocumentRepository.findByStatus(KycStatus.PENDING_REVIEW, pageable)
                .map(kycDocumentMapper::toDto);
    }

    /**
     * Récupère tous les documents d'un client
     */
    public List<KycDocumentDto> getCustomerDocuments(Integer customerId) {
        return kycDocumentRepository.findByCustomerIdOrderBySubmittedAtDesc(customerId)
                .stream()
                .map(kycDocumentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Compte les documents en attente
     */
    public long countPendingDocuments() {
        return kycDocumentRepository.countByStatus(KycStatus.PENDING_REVIEW);
    }

    /**
     * Vérifie si un client a un document KYC valide
     */
    public boolean hasValidKyc(Integer customerId) {
        return kycDocumentRepository.hasValidDocument(customerId, new Date());
    }

    /**
     * Marque les documents expirés
     */
    @Transactional
    public void markExpiredDocuments() {
        log.info("Checking for expired KYC documents");
        List<KycDocument> expired = kycDocumentRepository.findExpiredDocuments(new Date());

        for (KycDocument doc : expired) {
            doc.setStatus(KycStatus.EXPIRED);
            kycDocumentRepository.save(doc);

            // Mettre à jour le client
            Customer customer = doc.getCustomer();
            customer.setIdentityVerified(0);
            customerRepository.save(customer);

            // Notifier le client
            notifyCustomerExpired(customer);
        }

        log.info("Marked {} documents as expired", expired.size());
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private void validateFile(MultipartFile file, String fieldName) throws SendByOpException {
        if (file == null || file.isEmpty()) {
            throw new SendByOpException(ErrorInfo.VALIDATION_ERROR, fieldName + " est requis");
        }

        if (file.getSize() > maxFileSize) {
            throw new SendByOpException(ErrorInfo.VALIDATION_ERROR,
                    fieldName + " est trop volumineux (max 5MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new SendByOpException(ErrorInfo.VALIDATION_ERROR,
                    fieldName + " doit être une image (JPG, PNG)");
        }
    }

    private String saveFile(MultipartFile file, Integer customerId, String side) throws IOException {
        // Créer le répertoire si nécessaire
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String filename = String.format("kyc_%d_%s_%s%s",
                customerId, side, UUID.randomUUID().toString(), extension);

        Path filePath = uploadPath.resolve(filename);

        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    private void notifyAdminNewSubmission(Customer customer, KycDocument document) {
        try {
            String subject = "Nouveau document KYC à valider";
            String message = String.format(
                    "Un nouveau document KYC a été soumis par %s %s (%s).\n\n" +
                    "Type de document: %s\n" +
                    "Date de soumission: %s\n\n" +
                    "Veuillez vous connecter au panel admin pour le valider.",
                    customer.getFirstName(), customer.getLastName(), customer.getEmail(),
                    document.getDocumentType(), document.getSubmittedAt()
            );
            // Envoyer à l'admin (à configurer)
            log.info("Admin notification sent for KYC submission");
        } catch (Exception e) {
            log.error("Error sending admin notification", e);
        }
    }

    private void notifyCustomerApproved(Customer customer) {
        try {
            String subject = "Votre identité a été vérifiée ✓";
            String message = String.format(
                    "Bonjour %s,\n\n" +
                    "Bonne nouvelle ! Votre identité a été vérifiée avec succès.\n\n" +
                    "Vous pouvez maintenant :\n" +
                    "- Effectuer des réservations\n" +
                    "- Publier des voyages\n" +
                    "- Accéder à toutes les fonctionnalités de SendByOp\n\n" +
                    "Merci de votre confiance !\n\n" +
                    "L'équipe SendByOp",
                    customer.getFirstName()
            );
            emailService.sendHtmlEmail(customer.getEmail(), subject, message);
        } catch (Exception e) {
            log.error("Error sending approval notification", e);
        }
    }

    private void notifyCustomerRejected(Customer customer, String reason) {
        try {
            String subject = "Document d'identité rejeté";
            String message = String.format(
                    "Bonjour %s,\n\n" +
                    "Malheureusement, votre document d'identité n'a pas pu être validé.\n\n" +
                    "Motif: %s\n\n" +
                    "Veuillez soumettre un nouveau document depuis votre profil.\n\n" +
                    "L'équipe SendByOp",
                    customer.getFirstName(), reason
            );
            emailService.sendHtmlEmail(customer.getEmail(), subject, message);
        } catch (Exception e) {
            log.error("Error sending rejection notification", e);
        }
    }

    private void notifyCustomerExpired(Customer customer) {
        try {
            String subject = "Document d'identité expiré";
            String message = String.format(
                    "Bonjour %s,\n\n" +
                    "Votre document d'identité a expiré.\n\n" +
                    "Pour continuer à utiliser SendByOp, veuillez soumettre un nouveau document valide depuis votre profil.\n\n" +
                    "L'équipe SendByOp",
                    customer.getFirstName()
            );
            emailService.sendHtmlEmail(customer.getEmail(), subject, message);
        } catch (Exception e) {
            log.error("Error sending expiry notification", e);
        }
    }
}
