package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BookingResponseDto;
import com.sendByOP.expedition.models.dto.CreateBookingRequest;
import com.sendByOP.expedition.models.dto.CustomerBookingDto;
import com.sendByOP.expedition.models.dto.PaymentRequest;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.Transaction;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.repositories.TransactionRepository;
import com.sendByOP.expedition.services.iServices.IBookingService;
import com.sendByOP.expedition.services.impl.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * Contrôleur pour la gestion des réservations
 */
@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Gestion des réservations de transport de colis")
public class BookingController {

    private final IBookingService bookingService;
    private final InvoiceService invoiceService;
    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;

    /**
     * Créer une nouvelle réservation de transport de colis
     * 
     * <p>Cette API permet à un client authentifié de créer une réservation pour transporter
     * un colis sur un vol existant. Le processus inclut :</p>
     * <ul>
     *   <li>Validation du vol et de sa capacité disponible</li>
     *   <li>Upload de 1 à 5 photos du colis (obligatoire)</li>
     *   <li>Création ou récupération du destinataire</li>
     *   <li>Calcul automatique du prix si non fourni</li>
     *   <li>Création de la réservation avec statut PENDING_CONFIRMATION</li>
     * </ul>
     * 
     * <p><strong>Note importante :</strong> L'email du client est automatiquement récupéré 
     * depuis le token JWT de l'utilisateur authentifié. Il n'est pas nécessaire de le fournir.</p>
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Créer une nouvelle réservation de transport de colis",
            description = """
                    Crée une réservation pour transporter un colis sur un vol existant.
                    
                    **Processus :**
                    1. Validation du vol (existence, capacité disponible, statut VALIDATED)
                    2. Upload des photos du colis (1 à 5 photos requises, max 5MB chacune)
                    3. Création ou récupération du destinataire (basé sur email ou téléphone)
                    4. Calcul automatique du prix si non fourni (basé sur poids et distance)
                    5. Création de la réservation avec statut PENDING_CONFIRMATION
                    
                    **Authentification :**
                    L'email du client est automatiquement extrait du token JWT. 
                    Assurez-vous d'inclure le header : `Authorization: Bearer <token>`
                    
                    **Photos :**
                    - Formats acceptés : JPEG, PNG, WebP
                    - Taille max par photo : 5 MB
                    - Nombre : 1 à 5 photos (la première sera marquée comme principale)
                    
                    **Destinataire :**
                    Si un destinataire avec le même email ou téléphone existe déjà, 
                    il sera réutilisé. Sinon, un nouveau destinataire sera créé.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Réservation créée avec succès. Retourne les détails complets de la réservation incluant les photos uploadées.",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides : vol invalide, photos manquantes/trop nombreuses, poids hors limites, ou données destinataire incomplètes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié : token JWT manquant ou invalide",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès refusé : rôle CUSTOMER ou USER requis",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ressource non trouvée : vol inexistant ou client introuvable",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "Fichier trop volumineux : une ou plusieurs photos dépassent 5 MB",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur serveur : échec de l'upload des photos ou erreur lors de la création",
                    content = @Content
            )
    })
    public ResponseEntity<BookingResponseDto> createBooking(
            @Parameter(
                    description = "Données de la réservation (flightId, informations destinataire, détails du colis)",
                    required = true
            )
            @Valid @ModelAttribute CreateBookingRequest request,
            
            @Parameter(
                    description = "Photos du colis (1 à 5 photos, formats: JPEG/PNG/WebP, max 5MB chacune)",
                    required = true
            )
            @RequestParam("parcelPhotos") MultipartFile[] parcelPhotos,
            
            @Parameter(hidden = true)
            Authentication authentication
    ) throws SendByOpException {
        
        // Récupérer l'email de l'utilisateur connecté
        String email = authentication.getName();
        
        log.info("POST /api/bookings - Creating booking for customer {} on flight {} with {} photos", 
                email, request.getFlightId(), parcelPhotos.length);
        
        // Créer la réservation avec plusieurs photos
        BookingResponseDto booking = bookingService.createBooking(request, parcelPhotos, email);
        
        log.info("Booking created successfully: {} with {} photos", booking.getId(), parcelPhotos.length);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /**
     * Confirmer une réservation (voyageur)
     */
    @PutMapping("/{bookingId}/confirm")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Confirmer une réservation",
            description = "Permet au voyageur propriétaire du vol de confirmer une réservation en attente. " +
                    "Change le statut vers CONFIRMED_UNPAID et définit la deadline de paiement."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Réservation confirmée avec succès",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Statut invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé (pas le propriétaire du vol)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> confirmBooking(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId,
            
            @Parameter(description = "ID du voyageur")
            @RequestParam Integer travelerId
    ) throws SendByOpException {
        
        log.info("PUT /api/bookings/{}/confirm by traveler {}", bookingId, travelerId);
        
        BookingResponseDto booking = bookingService.confirmBooking(bookingId, travelerId);
        
        log.info("Booking {} confirmed successfully", bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Rejeter une réservation (voyageur)
     */
    @PutMapping("/{bookingId}/reject")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Rejeter une réservation",
            description = "Permet au voyageur propriétaire du vol de rejeter une réservation en attente. " +
                    "Change le statut vers CANCELLED_BY_TRAVELER."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Réservation rejetée avec succès",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Statut invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé (pas le propriétaire du vol)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> rejectBooking(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId,
            
            @Parameter(description = "ID du voyageur")
            @RequestParam Integer travelerId,
            
            @Parameter(description = "Raison du rejet (optionnel)")
            @RequestParam(required = false) String reason
    ) throws SendByOpException {
        
        log.info("PUT /api/bookings/{}/reject by traveler {}", bookingId, travelerId);
        
        BookingResponseDto booking = bookingService.rejectBooking(bookingId, travelerId, reason);
        
        log.info("Booking {} rejected successfully", bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Traiter le paiement d'une réservation (client)
     */
    @PostMapping("/{bookingId}/payment")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Payer une réservation",
            description = "Permet au client de payer une réservation confirmée. " +
                    "Change le statut vers CONFIRMED_PAID. Le montant doit correspondre au prix de la réservation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Paiement traité avec succès",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Données invalides ou montant incorrect", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé (pas le propriétaire de la réservation)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content),
            @ApiResponse(responseCode = "402", description = "Deadline de paiement dépassée", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> processPayment(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId,
            
            @Parameter(description = "Détails du paiement")
            @Valid @RequestBody PaymentRequest paymentRequest,
            
            @Parameter(description = "ID du client")
            @RequestParam Integer customerId
    ) throws SendByOpException {
        
        log.info("POST /api/bookings/{}/payment by customer {}", bookingId, customerId);
        
        BookingResponseDto booking = bookingService.processPayment(bookingId, paymentRequest, customerId);
        
        log.info("Payment processed successfully for booking {}", bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Annuler une réservation (client)
     */
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Annuler une réservation",
            description = "Permet au client d'annuler sa réservation. " +
                    "Possible pour les statuts: PENDING_CONFIRMATION, CONFIRMED_UNPAID, CONFIRMED_PAID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Réservation annulée avec succès",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Statut invalide pour annulation", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> cancelBooking(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId,
            
            @Parameter(description = "ID du client")
            @RequestParam Integer customerId,
            
            @Parameter(description = "Raison de l'annulation (optionnel)")
            @RequestParam(required = false) String reason
    ) throws SendByOpException {
        
        log.info("PUT /api/bookings/{}/cancel by customer {}", bookingId, customerId);
        
        BookingResponseDto booking = bookingService.cancelByClient(bookingId, customerId, reason);
        
        log.info("Booking {} cancelled successfully", bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Marquer comme livrée (voyageur)
     */
    @PutMapping("/{bookingId}/delivered")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Marquer une réservation comme livrée",
            description = "Permet au voyageur de marquer une réservation payée comme livrée"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Réservation marquée comme livrée",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Statut invalide (doit être payée)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé (pas le propriétaire du vol)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> markAsDelivered(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId,
            
            @Parameter(description = "ID du voyageur")
            @RequestParam Integer travelerId
    ) throws SendByOpException {
        
        log.info("PUT /api/bookings/{}/delivered by traveler {}", bookingId, travelerId);
        
        BookingResponseDto booking = bookingService.markAsDelivered(bookingId, travelerId);
        
        log.info("Booking {} marked as delivered", bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Marquer comme récupérée (client)
     */
    @PutMapping("/{bookingId}/picked-up")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Marquer une réservation comme récupérée",
            description = "Permet au client de confirmer qu'il a récupéré le colis"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Réservation marquée comme récupérée",
                    content = @Content(schema = @Schema(implementation = BookingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Statut invalide (doit être livrée)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> markAsPickedUp(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId,
            
            @Parameter(description = "ID du client")
            @RequestParam Integer customerId
    ) throws SendByOpException {
        
        log.info("PUT /api/bookings/{}/picked-up by customer {}", bookingId, customerId);
        
        BookingResponseDto booking = bookingService.markAsPickedUp(bookingId, customerId);
        
        log.info("Booking {} marked as picked up", bookingId);
        return ResponseEntity.ok(booking);
    }

    // ==========================================
    // ENDPOINTS DE LISTE DES RÉSERVATIONS CLIENT
    // ==========================================

    /**
     * Récupérer toutes les réservations d'un client par email
     */
    @GetMapping("/customer/{email}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Récupérer les réservations d'un client",
            description = "Retourne toutes les réservations d'un client identifié par son email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des réservations récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = CustomerBookingDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<List<CustomerBookingDto>> getCustomerBookingsByEmail(
            @Parameter(description = "Email du client")
            @PathVariable String email
    ) throws SendByOpException {
        
        log.info("GET /api/bookings/customer/{} - Fetching customer bookings", email);
        
        List<CustomerBookingDto> bookings = bookingService.getCustomerBookingsByEmail(email);
        
        log.info("Found {} bookings for customer {}", bookings.size(), email);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Récupérer les réservations d'un client avec pagination
     */
    @GetMapping("/customer/{email}/paginated")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Récupérer les réservations d'un client avec pagination",
            description = "Retourne une page de réservations d'un client identifié par son email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Page de réservations récupérée avec succès"
            ),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<Page<CustomerBookingDto>> getCustomerBookingsByEmailPaginated(
            @Parameter(description = "Email du client")
            @PathVariable String email,
            
            @Parameter(description = "Numéro de page (commence à 0)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "10") int size
    ) throws SendByOpException {
        
        log.info("GET /api/bookings/customer/{}/paginated?page={}&size={}", email, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerBookingDto> bookingsPage = bookingService.getCustomerBookingsByEmailPaginated(email, pageable);
        
        log.info("Found {} bookings (page {} of {}) for customer {}", 
                bookingsPage.getNumberOfElements(), page, bookingsPage.getTotalPages(), email);
        return ResponseEntity.ok(bookingsPage);
    }

    // ==========================================
    // ENDPOINTS POUR LE VOYAGEUR (TRAVELER)
    // ==========================================

    /**
     * Récupérer les réservations faites sur les vols du voyageur
     */
    @GetMapping("/traveler/{email}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Récupérer les réservations sur les vols du voyageur",
            description = "Retourne toutes les réservations faites par des clients sur les vols créés par ce voyageur"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des réservations récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = CustomerBookingDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<List<CustomerBookingDto>> getTravelerFlightBookings(
            @Parameter(description = "Email du voyageur")
            @PathVariable String email,
            Authentication authentication
    ) throws SendByOpException {
        
        // Vérifier que l'utilisateur authentifié est bien le voyageur
        if (!authentication.getName().equals(email)) {
            log.warn("User {} tried to access bookings for traveler {}", authentication.getName(), email);
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED, 
                    "Vous n'êtes pas autorisé à voir ces réservations");
        }
        
        log.info("GET /api/bookings/traveler/{} - Fetching bookings on traveler's flights", email);
        
        List<CustomerBookingDto> bookings = bookingService.getTravelerFlightBookings(email);
        
        log.info("Found {} bookings on flights for traveler {}", bookings.size(), email);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Récupérer les réservations sur les vols du voyageur avec pagination
     */
    @GetMapping("/traveler/{email}/paginated")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Récupérer les réservations sur les vols du voyageur avec pagination",
            description = "Retourne une page de réservations faites sur les vols de ce voyageur"
    )
    public ResponseEntity<Page<CustomerBookingDto>> getTravelerFlightBookingsPaginated(
            @Parameter(description = "Email du voyageur")
            @PathVariable String email,
            
            @Parameter(description = "Numéro de page (commence à 0)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "10") int size,
            
            Authentication authentication
    ) throws SendByOpException {
        
        // Vérifier que l'utilisateur authentifié est bien le voyageur
        if (!authentication.getName().equals(email)) {
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED, 
                    "Vous n'êtes pas autorisé à voir ces réservations");
        }
        
        log.info("GET /api/bookings/traveler/{}/paginated?page={}&size={}", email, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerBookingDto> bookingsPage = bookingService.getTravelerFlightBookingsPaginated(email, pageable);
        
        log.info("Found {} bookings (page {} of {}) on flights for traveler {}", 
                bookingsPage.getNumberOfElements(), page, bookingsPage.getTotalPages(), email);
        return ResponseEntity.ok(bookingsPage);
    }

    /**
     * Récupérer les détails d'une réservation (accessible par client ou voyageur)
     */
    @GetMapping("/{bookingId}/details")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Récupérer les détails complets d'une réservation",
            description = "Retourne les détails complets incluant les photos du colis. " +
                    "Accessible par le client qui a fait la réservation ou le voyageur du vol."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Détails de la réservation récupérés avec succès"
            ),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé à voir cette réservation", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content)
    })
    public ResponseEntity<CustomerBookingDto> getBookingDetails(
            @Parameter(description = "ID de la réservation")
            @PathVariable Integer bookingId,
            Authentication authentication
    ) throws SendByOpException {
        
        log.info("GET /api/bookings/{}/details by user {}", bookingId, authentication.getName());
        
        CustomerBookingDto booking = bookingService.getBookingDetails(bookingId, authentication.getName());
        
        return ResponseEntity.ok(booking);
    }
    
    /**
     * Le client marque qu'il a donné le colis au voyageur
     */
    @PutMapping("/{bookingId}/parcel-handed")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Marquer le colis comme remis au voyageur",
            description = "Le client confirme avoir donné le colis au voyageur. " +
                    "Envoie une notification au voyageur."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Colis marqué comme remis"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content),
            @ApiResponse(responseCode = "400", description = "Statut invalide", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> markParcelHandedToTraveler(
            @PathVariable Integer bookingId,
            @RequestParam Integer customerId,
            Authentication authentication
    ) throws SendByOpException {
        // Vérifier que l'utilisateur authentifié est bien le client
        // (le customerId est passé pour double vérification)
        
        log.info("PUT /api/bookings/{}/parcel-handed by customer {}", bookingId, customerId);
        
        BookingResponseDto response = bookingService.markParcelHandedToTraveler(bookingId, customerId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Le voyageur confirme avoir reçu le colis
     */
    @PutMapping("/{bookingId}/parcel-received")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Confirmer la réception du colis par le voyageur",
            description = "Le voyageur confirme avoir reçu le colis du client. " +
                    "Envoie une notification au client."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réception confirmée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content),
            @ApiResponse(responseCode = "400", description = "Statut invalide", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> confirmParcelReceived(
            @PathVariable Integer bookingId,
            @RequestParam Integer travelerId,
            Authentication authentication
    ) throws SendByOpException {
        
        log.info("PUT /api/bookings/{}/parcel-received by traveler {}", bookingId, travelerId);
        
        BookingResponseDto response = bookingService.confirmParcelReceivedByTraveler(bookingId, travelerId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Le voyageur confirme avoir remis le colis au destinataire
     */
    @PutMapping("/{bookingId}/parcel-delivered")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Confirmer la livraison du colis au destinataire",
            description = "Le voyageur confirme avoir remis le colis au destinataire. " +
                    "Envoie des notifications au client et au destinataire."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Livraison confirmée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée", content = @Content),
            @ApiResponse(responseCode = "400", description = "Statut invalide", content = @Content)
    })
    public ResponseEntity<BookingResponseDto> confirmParcelDelivered(
            @PathVariable Integer bookingId,
            @RequestParam Integer travelerId,
            Authentication authentication
    ) throws SendByOpException {
        
        log.info("PUT /api/bookings/{}/parcel-delivered by traveler {}", bookingId, travelerId);
        
        BookingResponseDto response = bookingService.confirmParcelDeliveredToReceiver(bookingId, travelerId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Télécharge le reçu/facture pour une réservation
     */
    @GetMapping("/{bookingId}/receipt")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    @Operation(
            summary = "Télécharger le reçu d'une réservation",
            description = "Génère et télécharge le reçu/facture pour une réservation payée"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reçu téléchargé avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation ou transaction non trouvée", content = @Content)
    })
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable @Parameter(description = "ID de la réservation") Integer bookingId,
            Principal principal) {
        
        log.info("Téléchargement du reçu pour la réservation: {} par {}", 
                bookingId, principal.getName());
        
        try {
            // Récupérer la réservation
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                            "Réservation non trouvée"));
            
            // Vérifier que la réservation appartient bien au client connecté
            if (!booking.getCustomer().getEmail().equals(principal.getName())) {
                log.warn("Tentative d'accès non autorisé au reçu de la réservation {} par {}", 
                        bookingId, principal.getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Trouver la transaction associée à cette réservation
            Transaction transaction = transactionRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
                    .stream()
                    .filter(t -> "COMPLETED".equals(t.getStatus().name()) || 
                                 "SUCCESS".equals(t.getStatus().name()))
                    .findFirst()
                    .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                            "Aucune transaction payée trouvée pour cette réservation"));
            
            // Générer la facture
            byte[] invoicePdf = invoiceService.generateInvoice(transaction);
            
            // Préparer les headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "recu_reservation_" + bookingId + ".pdf");
            headers.setContentLength(invoicePdf.length);
            
            log.info("Reçu généré et prêt au téléchargement pour la réservation: {}", bookingId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(invoicePdf);
            
        } catch (SendByOpException e) {
            log.error("Erreur lors du téléchargement du reçu: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            
        } catch (Exception e) {
            log.error("Erreur inattendue lors du téléchargement du reçu: {}", 
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
