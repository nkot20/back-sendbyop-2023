package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.KycDocument;
import com.sendByOP.expedition.models.enums.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Integer> {
    
    /**
     * Trouve le dernier document KYC d'un client
     */
    Optional<KycDocument> findFirstByCustomerIdOrderBySubmittedAtDesc(Integer customerId);
    
    /**
     * Trouve tous les documents d'un client
     */
    List<KycDocument> findByCustomerIdOrderBySubmittedAtDesc(Integer customerId);
    
    /**
     * Trouve les documents par statut
     */
    Page<KycDocument> findByStatus(KycStatus status, Pageable pageable);
    
    /**
     * Compte les documents en attente de validation
     */
    long countByStatus(KycStatus status);
    
    /**
     * Trouve les documents expirés qui sont encore approuvés
     */
    @Query("SELECT k FROM KycDocument k WHERE k.status = 'APPROVED' AND k.expiryDate < :currentDate")
    List<KycDocument> findExpiredDocuments(@Param("currentDate") Date currentDate);
    
    /**
     * Vérifie si un client a un document valide
     */
    @Query("SELECT CASE WHEN COUNT(k) > 0 THEN true ELSE false END FROM KycDocument k " +
           "WHERE k.customer.id = :customerId " +
           "AND k.status = 'APPROVED' " +
           "AND (k.expiryDate IS NULL OR k.expiryDate >= :currentDate)")
    boolean hasValidDocument(@Param("customerId") Integer customerId, @Param("currentDate") Date currentDate);
}
