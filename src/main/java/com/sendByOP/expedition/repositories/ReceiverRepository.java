package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des destinataires
 * Permet le contrôle de doublons sur email et numéro de téléphone
 */
@Repository
public interface ReceiverRepository extends JpaRepository<Receiver, Integer> {
    
    /**
     * Recherche un destinataire par email
     * @param email Email du destinataire
     * @return Optional contenant le destinataire si trouvé
     */
    Optional<Receiver> findByEmail(String email);
    
    /**
     * Recherche un destinataire par numéro de téléphone
     * @param phoneNumber Numéro de téléphone du destinataire
     * @return Optional contenant le destinataire si trouvé
     */
    Optional<Receiver> findByPhoneNumber(String phoneNumber);
    
    /**
     * Vérifie si un destinataire existe avec cet email
     * @param email Email à vérifier
     * @return true si existe, false sinon
     */
    Boolean existsByEmail(String email);
    
    /**
     * Vérifie si un destinataire existe avec ce numéro
     * @param phoneNumber Numéro à vérifier
     * @return true si existe, false sinon
     */
    Boolean existsByPhoneNumber(String phoneNumber);
}
