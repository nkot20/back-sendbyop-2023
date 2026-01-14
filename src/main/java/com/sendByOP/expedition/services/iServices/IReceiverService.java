package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.ReceiverDto;

/**
 * Service de gestion des destinataires de colis
 * Gère la création, récupération et contrôle des doublons
 */
public interface IReceiverService {
    
    /**
     * Crée un nouveau destinataire
     * Vérifie qu'il n'existe pas déjà un destinataire avec le même email ou téléphone
     * 
     * @param receiverDto Données du destinataire à créer
     * @return Le destinataire créé avec son ID
     * @throws SendByOpException Si les données sont invalides ou si un doublon existe
     */
    ReceiverDto createReceiver(ReceiverDto receiverDto) throws SendByOpException;
    
    /**
     * Récupère un destinataire par son adresse email
     * 
     * @param email Email du destinataire
     * @return Le destinataire trouvé
     * @throws SendByOpException Si le destinataire n'existe pas
     */
    ReceiverDto getReceiverByEmail(String email) throws SendByOpException;
    
    /**
     * Récupère un destinataire par son numéro de téléphone
     * 
     * @param phoneNumber Numéro de téléphone du destinataire
     * @return Le destinataire trouvé
     * @throws SendByOpException Si le destinataire n'existe pas
     */
    ReceiverDto getReceiverByPhoneNumber(String phoneNumber) throws SendByOpException;
    
    /**
     * Récupère un destinataire existant ou le crée s'il n'existe pas
     * La recherche se fait par email OU par numéro de téléphone
     * 
     * Logique:
     * 1. Si email fourni → chercher par email
     * 2. Sinon si téléphone fourni → chercher par téléphone
     * 3. Si aucun trouvé → créer nouveau destinataire
     * 
     * @param receiverDto Données du destinataire
     * @return Le destinataire existant ou nouvellement créé
     * @throws SendByOpException Si les données sont invalides
     */
    ReceiverDto getOrCreateReceiver(ReceiverDto receiverDto) throws SendByOpException;
    
    /**
     * Met à jour les informations d'un destinataire existant
     * 
     * @param receiverDto Données du destinataire à mettre à jour (avec ID)
     * @return Le destinataire mis à jour
     * @throws SendByOpException Si le destinataire n'existe pas ou si les données sont invalides
     */
    ReceiverDto updateReceiver(ReceiverDto receiverDto) throws SendByOpException;
    
    /**
     * Vérifie si un destinataire existe avec l'email ou le téléphone donné
     * 
     * @param email Email à vérifier (peut être null)
     * @param phoneNumber Numéro de téléphone à vérifier (peut être null)
     * @return true si un destinataire existe, false sinon
     */
    boolean receiverExists(String email, String phoneNumber);
}
