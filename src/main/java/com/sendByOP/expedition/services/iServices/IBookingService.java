package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BookingResponseDto;
import com.sendByOP.expedition.models.dto.CreateBookingRequest;
import com.sendByOP.expedition.models.dto.CustomerBookingDto;
import com.sendByOP.expedition.models.dto.PaymentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service de gestion des réservations de transport de colis
 */
public interface IBookingService {
    
    /**
     * Crée une nouvelle réservation
     * 
     * Processus:
     * 1. Valider les données de la requête
     * 2. Vérifier que le vol existe et est disponible
     * 3. Vérifier que le client existe
     * 4. GetOrCreate le destinataire (via ReceiverService)
     * 5. Uploader la photo du colis
     * 6. Calculer le prix (via PlatformSettings ou prix proposé si valide)
     * 7. Créer la réservation avec status PENDING_CONFIRMATION
     * 8. Retourner les détails de la réservation
     * 
     * @param request Données de la réservation
     * @param parcelPhotos Photos du colis (1 à 5 photos requises)
     * @param customerId email du client qui fait la réservation
     * @return Les détails de la réservation créée
     * @throws SendByOpException Si les données sont invalides ou si une ressource n'existe pas
     */
    BookingResponseDto createBooking(
            CreateBookingRequest request,
            MultipartFile[] parcelPhotos,
            String customerId
    ) throws SendByOpException;
    
    /**
     * Confirme une réservation (action du voyageur)
     * 
     * Processus:
     * 1. Vérifier que la réservation existe
     * 2. Vérifier que le voyageur est propriétaire du vol
     * 3. Vérifier que le statut est PENDING_CONFIRMATION
     * 4. Mettre à jour le statut vers CONFIRMED_UNPAID
     * 5. Définir la deadline de paiement (selon PlatformSettings)
     * 6. Enregistrer la date de confirmation
     * 7. [Future] Envoyer notification au client
     * 
     * @param bookingId ID de la réservation
     * @param travelerId ID du voyageur (propriétaire du vol)
     * @return Les détails de la réservation confirmée
     * @throws SendByOpException Si la réservation n'existe pas, si le voyageur n'est pas autorisé, ou si le statut est invalide
     */
    BookingResponseDto confirmBooking(Integer bookingId, Integer travelerId) throws SendByOpException;
    
    /**
     * Rejette une réservation (action du voyageur)
     * 
     * Processus:
     * 1. Vérifier que la réservation existe
     * 2. Vérifier que le voyageur est propriétaire du vol
     * 3. Vérifier que le statut est PENDING_CONFIRMATION
     * 4. Mettre à jour le statut vers CANCELLED_BY_TRAVELER
     * 5. Enregistrer la raison du rejet
     * 6. [Future] Envoyer notification au client
     * 
     * @param bookingId ID de la réservation
     * @param travelerId ID du voyageur (propriétaire du vol)
     * @param reason Raison du rejet (optionnel)
     * @return Les détails de la réservation rejetée
     * @throws SendByOpException Si la réservation n'existe pas, si le voyageur n'est pas autorisé, ou si le statut est invalide
     */
    BookingResponseDto rejectBooking(Integer bookingId, Integer travelerId, String reason) throws SendByOpException;
    
    /**
     * Traite le paiement d'une réservation (action du client)
     * 
     * Processus:
     * 1. Vérifier que la réservation existe
     * 2. Vérifier que le client est propriétaire de la réservation
     * 3. Vérifier que le statut est CONFIRMED_UNPAID
     * 4. Vérifier que la deadline de paiement n'est pas dépassée
     * 5. Valider le montant (doit correspondre au totalPrice)
     * 6. [Future] Traiter le paiement via gateway (Stripe/PayPal)
     * 7. Mettre à jour le statut vers CONFIRMED_PAID
     * 8. Enregistrer les détails du paiement
     * 9. [Future] Envoyer notifications (client + voyageur)
     * 
     * @param bookingId ID de la réservation
     * @param paymentRequest Détails du paiement
     * @param customerId ID du client
     * @return Les détails de la réservation payée
     * @throws SendByOpException Si la réservation n'existe pas, si le client n'est pas autorisé, 
     *                           si le statut est invalide, si la deadline est dépassée, ou si le montant est incorrect
     */
    BookingResponseDto processPayment(Integer bookingId, PaymentRequest paymentRequest, Integer customerId) throws SendByOpException;
    
    /**
     * Annule une réservation (action du client)
     * 
     * Processus:
     * 1. Vérifier que la réservation existe
     * 2. Vérifier que le client est propriétaire de la réservation
     * 3. Vérifier que le statut permet l'annulation (PENDING_CONFIRMATION, CONFIRMED_UNPAID, ou CONFIRMED_PAID)
     * 4. Mettre à jour le statut vers CANCELLED_BY_CLIENT
     * 5. Enregistrer la raison de l'annulation
     * 6. [Future] Calculer et appliquer les pénalités si applicable
     * 7. [Future] Envoyer notifications
     * 
     * @param bookingId ID de la réservation
     * @param customerId ID du client
     * @param reason Raison de l'annulation (optionnel)
     * @return Les détails de la réservation annulée
     * @throws SendByOpException Si la réservation n'existe pas, si le client n'est pas autorisé, ou si l'annulation n'est pas permise
     */
    BookingResponseDto cancelByClient(Integer bookingId, Integer customerId, String reason) throws SendByOpException;
    
    /**
     * Annule automatiquement les réservations non payées dont la deadline est dépassée
     * 
     * Processus:
     * 1. Rechercher toutes les réservations avec status CONFIRMED_UNPAID
     * 2. Filtrer celles dont paymentDeadline < now
     * 3. Mettre à jour le statut vers CANCELLED_PAYMENT_TIMEOUT
     * 4. [Future] Envoyer notifications client + voyageur
     * 
     * Cette méthode est appelée par un job cron (@Scheduled)
     * 
     * @return Nombre de réservations annulées
     */
    int autoCancelUnpaidBookings();
    
    /**
     * Marque une réservation comme livrée (action du voyageur)
     * 
     * Processus:
     * 1. Vérifier que la réservation existe
     * 2. Vérifier que le voyageur est propriétaire du vol
     * 3. Vérifier que le statut est CONFIRMED_PAID
     * 4. Mettre à jour le statut vers DELIVERED
     * 5. Enregistrer la date de livraison
     * 6. [Future] Envoyer notification au client
     * 
     * @param bookingId ID de la réservation
     * @param travelerId ID du voyageur
     * @return Les détails de la réservation livrée
     * @throws SendByOpException Si la réservation n'existe pas, si le voyageur n'est pas autorisé, ou si le statut est invalide
     */
    BookingResponseDto markAsDelivered(Integer bookingId, Integer travelerId) throws SendByOpException;
    
    /**
     * Marque une réservation comme récupérée (action du client)
     * 
     * Processus:
     * 1. Vérifier que la réservation existe
     * 2. Vérifier que le client est propriétaire de la réservation
     * 3. Vérifier que le statut est DELIVERED
     * 4. Mettre à jour le statut vers PICKED_UP
     * 5. Enregistrer la date de récupération
     * 6. [Future] Déclencher processus de paiement au voyageur
     * 7. [Future] Envoyer notifications
     * 
     * @param bookingId ID de la réservation
     * @param customerId ID du client
     * @return Les détails de la réservation récupérée
     * @throws SendByOpException Si la réservation n'existe pas, si le client n'est pas autorisé, ou si le statut est invalide
     */
    BookingResponseDto markAsPickedUp(Integer bookingId, Integer customerId) throws SendByOpException;
    
    /**
     * Récupère toutes les réservations d'un client par email
     * 
     * @param email Email du client
     * @return Liste des réservations du client
     * @throws SendByOpException Si le client n'existe pas
     */
    List<CustomerBookingDto> getCustomerBookingsByEmail(String email) throws SendByOpException;
    
    /**
     * Récupère les réservations d'un client par email avec pagination
     * 
     * @param email Email du client
     * @param pageable Paramètres de pagination
     * @return Page de réservations du client
     * @throws SendByOpException Si le client n'existe pas
     */
    Page<CustomerBookingDto> getCustomerBookingsByEmailPaginated(String email, Pageable pageable) throws SendByOpException;
    
    /**
     * Récupère toutes les réservations faites sur les vols d'un voyageur
     * 
     * @param travelerEmail Email du voyageur (propriétaire des vols)
     * @return Liste des réservations sur les vols du voyageur
     * @throws SendByOpException Si le voyageur n'existe pas
     */
    List<CustomerBookingDto> getTravelerFlightBookings(String travelerEmail) throws SendByOpException;
    
    /**
     * Récupère les réservations faites sur les vols d'un voyageur avec pagination
     * 
     * @param travelerEmail Email du voyageur
     * @param pageable Paramètres de pagination
     * @return Page de réservations sur les vols du voyageur
     * @throws SendByOpException Si le voyageur n'existe pas
     */
    Page<CustomerBookingDto> getTravelerFlightBookingsPaginated(String travelerEmail, Pageable pageable) throws SendByOpException;
    
    /**
     * Récupère les détails d'une réservation avec les photos du colis
     * 
     * @param bookingId ID de la réservation
     * @param requesterId ID de l'utilisateur qui fait la requête (voyageur ou client)
     * @return Détails complets de la réservation
     * @throws SendByOpException Si la réservation n'existe pas ou si l'utilisateur n'est pas autorisé
     */
    CustomerBookingDto getBookingDetails(Integer bookingId, String requesterId) throws SendByOpException;
    
    /**
     * Le client marque qu'il a donné le colis au voyageur
     * 
     * @param bookingId ID de la réservation
     * @param customerId ID du client
     * @return Les détails de la réservation mise à jour
     * @throws SendByOpException Si la réservation n'existe pas ou si l'utilisateur n'est pas autorisé
     */
    BookingResponseDto markParcelHandedToTraveler(Integer bookingId, Integer customerId) throws SendByOpException;
    
    /**
     * Le voyageur confirme avoir reçu le colis
     * 
     * @param bookingId ID de la réservation
     * @param travelerId ID du voyageur
     * @return Les détails de la réservation mise à jour
     * @throws SendByOpException Si la réservation n'existe pas ou si l'utilisateur n'est pas autorisé
     */
    BookingResponseDto confirmParcelReceivedByTraveler(Integer bookingId, Integer travelerId) throws SendByOpException;
    
    /**
     * Le voyageur confirme avoir remis le colis au destinataire
     * 
     * @param bookingId ID de la réservation
     * @param travelerId ID du voyageur
     * @return Les détails de la réservation mise à jour
     * @throws SendByOpException Si la réservation n'existe pas ou si l'utilisateur n'est pas autorisé
     */
    BookingResponseDto confirmParcelDeliveredToReceiver(Integer bookingId, Integer travelerId) throws SendByOpException;
}
