package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.models.enums.BookingStatus;
import com.sendByOP.expedition.repositories.*;
import com.sendByOP.expedition.services.FileStorageService;
import com.sendByOP.expedition.services.iServices.IBookingService;
import com.sendByOP.expedition.services.iServices.IPlatformSettingsService;
import com.sendByOP.expedition.services.iServices.IReceiverService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service de gestion des réservations de transport de colis
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@Validated
public class BookingService implements IBookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final CustomerRepository customerRepository;
    private final ReceiverRepository receiverRepository;
    private final IReceiverService receiverService;
    private final IPlatformSettingsService platformSettingsService;
    private final FileStorageService fileStorageService;

    @Override
    public BookingResponseDto createBooking(
            @Valid CreateBookingRequest request,
            MultipartFile[] parcelPhotos,
            String customerId) throws SendByOpException {
        
        log.info("Creating booking for customer {} on flight {} with {} photos", 
                customerId, request.getFlightId(), parcelPhotos != null ? parcelPhotos.length : 0);
        
        // 1. Validation photos requises (1 à 5)
        if (parcelPhotos == null || parcelPhotos.length == 0) {
            log.error("At least one parcel photo is required");
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Au moins une photo du colis est requise");
        }
        
        if (parcelPhotos.length > 5) {
            log.error("Too many photos: {}", parcelPhotos.length);
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Maximum 5 photos autorisées (reçu: " + parcelPhotos.length + ")");
        }
        
        // 2. Vérifier que le client existe
        Customer customer = customerRepository.findByEmail(customerId)
                .orElseThrow(() -> {
                    log.error("Customer not found: {}", customerId);
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Client non trouvé");
                });
        
        // 3. Vérifier que le vol existe
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> {
                    log.error("Flight not found: {}", request.getFlightId());
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Vol non trouvé");
                });
        
        // 4. GetOrCreate destinataire
        ReceiverDto receiverDto = ReceiverDto.builder()
                .firstName(request.getReceiverFirstName())
                .lastName(request.getReceiverLastName())
                .email(request.getReceiverEmail())
                .phoneNumber(request.getReceiverPhoneNumber())
                .address(request.getReceiverAddress())
                .city(request.getReceiverCity())
                .country(request.getReceiverCountry())
                .build();
        
        ReceiverDto receiver = receiverService.getOrCreateReceiver(receiverDto);
        log.debug("Receiver: {}", receiver.getId());
        
        // 5. Upload photos du colis (dans le dossier parcel-photos)
        List<String> photoUrls = Arrays.stream(parcelPhotos)
                .filter(photo -> !photo.isEmpty())  // Filtrer les photos vides
                .map(photo -> {
                    try {
                        String photoUrl = fileStorageService.storeParcelPhoto(photo, customer.getId());
                        log.debug("Parcel photo uploaded: {}", photoUrl);
                        return photoUrl;
                    } catch (Exception e) {
                        log.error("Failed to upload parcel photo", e);
                        throw new RuntimeException("Erreur lors de l'upload d'une photo du colis: " + e.getMessage(), e);
                    }
                })
                .collect(Collectors.toList());
        
        if (photoUrls.isEmpty()) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Aucune photo valide n'a pu être uploadée");
        }
        
        log.info("{} photo(s) uploaded successfully", photoUrls.size());
        
        // 6. Calculer le prix basé sur le prix du vol
        BigDecimal totalPrice = calculatePrice(request, flight);
        log.info("Calculated price: {} € (weight: {} kg, flight price/kg: {} €)", 
                totalPrice, request.getParcelWeight(), flight.getAmountPerKg());
        
        // 7. Créer la réservation
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setFlight(flight);
        
        // Récupérer l'entité Receiver depuis la BD en utilisant l'ID retourné par getOrCreateReceiver
        Receiver receiverEntity = receiverRepository.findById(receiver.getId())
                .orElseThrow(() -> {
                    log.error("Receiver not found with ID: {} (should have been created)", receiver.getId());
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                            "Destinataire non trouvé après création");
                });
        
        log.debug("Receiver entity loaded: id={}, name={} {}", 
                receiverEntity.getId(), receiverEntity.getFirstName(), receiverEntity.getLastName());
        
        booking.setReceiver(receiverEntity);
        booking.setStatus(BookingStatus.PENDING_CONFIRMATION);
        booking.setBookingDate(new Date());
        booking.setTotalPrice(totalPrice);
        
        // Pour compatibilité: définir la première photo comme URL principale
        booking.setParcelPhotoUrl(photoUrls.get(0));
        
        // 7bis. Créer l'entité Parcel avec le poids et la description
        Parcel parcel = Parcel.builder()
                .description(request.getParcelDescription())
                .weightKg(request.getParcelWeight() != null ? request.getParcelWeight().floatValue() : null)
                .parcelType(request.getParcelCategory())
                .reservation(booking)
                .build();
        
        // Associer le colis à la réservation
        booking.setParcels(Collections.singletonList(parcel));
        log.debug("Parcel created: weight={} kg, description={}", parcel.getWeightKg(), parcel.getDescription());
        
        // 7ter. Créer les entités ParcelPhoto (approche fonctionnelle)
        List<ParcelPhoto> parcelPhotoEntities = IntStream.range(0, photoUrls.size())
                .mapToObj(index -> ParcelPhoto.builder()
                        .photoUrl(photoUrls.get(index))
                        .displayOrder(index)
                        .isPrimary(index == 0)  // La première est la principale
                        .booking(booking)
                        .build())
                .collect(Collectors.toList());
        
        // Associer les photos à la réservation (cascade sauvera automatiquement)
        booking.setParcelPhotos(parcelPhotoEntities);
        
        // Sauvegarder (cascade sauvera le parcel et les photos automatiquement)
        Booking saved = bookingRepository.save(booking);
        log.info("Booking created successfully: {}", saved.getId());
        
        // 8. Construire la réponse avec les photos
        List<ParcelPhotoDto> photoDtos = saved.getParcelPhotos().stream()
                .map(photo -> ParcelPhotoDto.builder()
                        .id(photo.getId())
                        .photoUrl(photo.getPhotoUrl())
                        .description(photo.getDescription())
                        .displayOrder(photo.getDisplayOrder())
                        .isPrimary(photo.getIsPrimary())
                        .build())
                .collect(Collectors.toList());
        
        return BookingResponseDto.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .bookingDate(saved.getBookingDate())
                .confirmedAt(saved.getConfirmedAt())
                .paymentDeadline(saved.getPaymentDeadline())
                .totalPrice(saved.getTotalPrice())
                .flightId(flight.getFlightId())
                .customerId(customer.getId())
                .receiverId(receiver.getId())
                .parcelPhotos(photoDtos)  // Nouvelle liste de photos
                .parcelPhotoUrl(saved.getParcelPhotoUrl())  // Compatibilité
                .receiverFullName(receiver.getFirstName() + " " + receiver.getLastName())
                .receiverEmail(receiver.getEmail())
                .receiverPhoneNumber(receiver.getPhoneNumber())
                .parcelWeight(request.getParcelWeight())
                .parcelDescription(request.getParcelDescription())
                .build();
    }
    
    /**
     * Calcule le prix de la réservation
     * Utilise le prix proposé s'il est valide, sinon calcule selon le prix du vol
     */
    private BigDecimal calculatePrice(CreateBookingRequest request, Flight flight) throws SendByOpException {
        BigDecimal weight = request.getParcelWeight();
        
        // Prix minimum = prix par kg du vol * poids
        BigDecimal flightPricePerKg = flight.getAmountPerKg() != null ? 
                BigDecimal.valueOf(flight.getAmountPerKg()) : BigDecimal.ZERO;
        BigDecimal flightPrice = flightPricePerKg.multiply(weight);
        
        log.debug("Price calculation: weight={} kg, pricePerKg={} €/kg, flightPrice={} €", 
                weight, flightPricePerKg, flightPrice);
        
        // Si un prix est proposé par le client
        if (request.getProposedPrice() != null && request.getProposedPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal proposedPrice = request.getProposedPrice();
            
            // Vérifier que le prix proposé n'est pas inférieur au prix du vol
            if (proposedPrice.compareTo(flightPrice) < 0) {
                log.warn("Proposed price {} is lower than flight price {}, using flight price", 
                        proposedPrice, flightPrice);
                return flightPrice;
            }
            
            log.debug("Using proposed price: {} €", proposedPrice);
            return proposedPrice;
        }
        
        // Sinon, utiliser le prix du vol
        log.debug("No proposed price, using flight price: {} €", flightPrice);
        return flightPrice;
    }
    
    @Override
    public BookingResponseDto confirmBooking(Integer bookingId, Integer travelerId) throws SendByOpException {
        log.info("Confirming booking {} by traveler {}", bookingId, travelerId);
        
        // 1. Vérifier que la réservation existe
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Réservation non trouvée");
                });
        
        // 2. Vérifier que le voyageur est propriétaire du vol
        Integer flightOwnerId = booking.getFlight().getCustomer().getId();
        if (!flightOwnerId.equals(travelerId)) {
            log.error("Traveler {} is not owner of flight {}", travelerId, booking.getFlight().getFlightId());
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED,
                    "Vous n'êtes pas autorisé à confirmer cette réservation");
        }
        
        // 3. Vérifier que le statut est PENDING_CONFIRMATION
        if (booking.getStatus() != BookingStatus.PENDING_CONFIRMATION) {
            log.error("Invalid status for confirmation: {}", booking.getStatus());
            throw new SendByOpException(ErrorInfo.INVALID_STATUS,
                    "La réservation ne peut être confirmée dans son état actuel");
        }
        
        // 4. Mettre à jour le statut vers CONFIRMED_UNPAID
        booking.setStatus(BookingStatus.CONFIRMED_UNPAID);
        
        // 5. Définir la deadline de paiement (selon PlatformSettings)
        PlatformSettingsDto settings = platformSettingsService.getSettings();
        LocalDateTime paymentDeadline = LocalDateTime.now()
                .plusHours(settings.getPaymentTimeoutHours());
        booking.setPaymentDeadline(paymentDeadline);
        
        // 6. Enregistrer la date de confirmation
        booking.setConfirmedAt(LocalDateTime.now());
        
        // Sauvegarder
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} confirmed successfully", bookingId);
        
        // Retourner la réponse
        return buildBookingResponse(saved);
    }
    
    @Override
    public BookingResponseDto rejectBooking(Integer bookingId, Integer travelerId, String reason) throws SendByOpException {
        log.info("Rejecting booking {} by traveler {}", bookingId, travelerId);
        
        // 1. Vérifier que la réservation existe
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Réservation non trouvée");
                });
        
        // 2. Vérifier que le voyageur est propriétaire du vol
        Integer flightOwnerId = booking.getFlight().getCustomer().getId();
        if (!flightOwnerId.equals(travelerId)) {
            log.error("Traveler {} is not owner of flight {}", travelerId, booking.getFlight().getFlightId());
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED,
                    "Vous n'êtes pas autorisé à rejeter cette réservation");
        }
        
        // 3. Vérifier que le statut est PENDING_CONFIRMATION
        if (booking.getStatus() != BookingStatus.PENDING_CONFIRMATION) {
            log.error("Invalid status for rejection: {}", booking.getStatus());
            throw new SendByOpException(ErrorInfo.INVALID_STATUS,
                    "La réservation ne peut être rejetée dans son état actuel");
        }
        
        // 4. Mettre à jour le statut vers CANCELLED_BY_TRAVELER
        booking.setStatus(BookingStatus.CANCELLED_BY_TRAVELER);
        
        // 5. Enregistrer la raison du rejet
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setCancellationReason(reason);
        }
        
        // Sauvegarder
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} rejected successfully", bookingId);
        
        // Retourner la réponse
        return buildBookingResponse(saved);
    }
    
    @Override
    public BookingResponseDto processPayment(Integer bookingId, PaymentRequest paymentRequest, Integer customerId) throws SendByOpException {
        log.info("Processing payment for booking {} by customer {}", bookingId, customerId);
        
        // 1. Vérifier que la réservation existe
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Réservation non trouvée");
                });
        
        // 2. Vérifier que le client est propriétaire de la réservation
        if (!booking.getCustomer().getId().equals(customerId)) {
            log.error("Customer {} is not owner of booking {}", customerId, bookingId);
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED,
                    "Vous n'êtes pas autorisé à payer cette réservation");
        }
        
        // 3. Vérifier que le statut est CONFIRMED_UNPAID
        if (booking.getStatus() != BookingStatus.CONFIRMED_UNPAID) {
            log.error("Invalid status for payment: {}", booking.getStatus());
            throw new SendByOpException(ErrorInfo.INVALID_STATUS,
                    "La réservation ne peut être payée dans son état actuel");
        }
        
        // 4. Vérifier que la deadline de paiement n'est pas dépassée
        if (booking.getPaymentDeadline() != null && 
                LocalDateTime.now().isAfter(booking.getPaymentDeadline())) {
            log.error("Payment deadline exceeded for booking {}", bookingId);
            throw new SendByOpException(ErrorInfo.PAYMENT_FAILED,
                    "Le délai de paiement est dépassé");
        }
        
        // 5. Valider le montant (doit correspondre au totalPrice)
        if (paymentRequest.getAmount().compareTo(booking.getTotalPrice()) != 0) {
            log.error("Payment amount mismatch: {} vs {}", 
                    paymentRequest.getAmount(), booking.getTotalPrice());
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                    "Le montant du paiement ne correspond pas au prix de la réservation");
        }
        
        // 6. [Future] Traiter le paiement via gateway (Stripe/PayPal)
        // Pour l'instant, on considère le paiement comme réussi
        log.debug("Payment method: {}", paymentRequest.getPaymentMethod());
        
        // 7. Mettre à jour le statut vers CONFIRMED_PAID
        booking.setStatus(BookingStatus.CONFIRMED_PAID);
        
        // 8. Enregistrer les détails du paiement
        // Note: Ces informations devraient normalement être dans une entité Payment séparée
        log.debug("Payment processed successfully for booking {}", bookingId);
        
        // Sauvegarder
        Booking saved = bookingRepository.save(booking);
        log.info("Payment processed successfully for booking {}", bookingId);
        
        // Retourner la réponse
        return buildBookingResponse(saved);
    }
    
    @Override
    public BookingResponseDto cancelByClient(Integer bookingId, Integer customerId, String reason) throws SendByOpException {
        log.info("Cancelling booking {} by client {}", bookingId, customerId);
        
        // 1. Vérifier que la réservation existe
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Réservation non trouvée");
                });
        
        // 2. Vérifier que le client est propriétaire de la réservation
        if (!booking.getCustomer().getId().equals(customerId)) {
            log.error("Customer {} is not owner of booking {}", customerId, bookingId);
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED,
                    "Vous n'êtes pas autorisé à annuler cette réservation");
        }
        
        // 3. Vérifier que le statut permet l'annulation
        if (booking.getStatus() == BookingStatus.DELIVERED || 
            booking.getStatus() == BookingStatus.PICKED_UP ||
            booking.getStatus() == BookingStatus.CANCELLED_BY_CLIENT ||
            booking.getStatus() == BookingStatus.CANCELLED_BY_TRAVELER ||
            booking.getStatus() == BookingStatus.CANCELLED_PAYMENT_TIMEOUT) {
            log.error("Cannot cancel booking in status: {}", booking.getStatus());
            throw new SendByOpException(ErrorInfo.INVALID_STATUS,
                    "La réservation ne peut pas être annulée dans son état actuel");
        }
        
        // 4. Mettre à jour le statut vers CANCELLED_BY_CLIENT
        booking.setStatus(BookingStatus.CANCELLED_BY_CLIENT);
        
        // 5. Enregistrer la raison de l'annulation
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setCancellationReason(reason);
        }
        
        // Sauvegarder
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} cancelled successfully by client", bookingId);
        
        // Retourner la réponse
        return buildBookingResponse(saved);
    }
    
    @Override
    public int autoCancelUnpaidBookings() {
        log.info("Running auto-cancellation of unpaid bookings");
        
        LocalDateTime now = LocalDateTime.now();
        int cancelledCount = 0;
        
        try {
            // Utilise la requête optimisée du repository
            List<Booking> expiredBookings = bookingRepository.findUnpaidWithExpiredDeadline(
                    BookingStatus.CONFIRMED_UNPAID,
                    now
            );
            
            log.debug("Found {} bookings with expired deadline", expiredBookings.size());
            
            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.CANCELLED_PAYMENT_TIMEOUT);
                bookingRepository.save(booking);
                cancelledCount++;
                log.debug("Auto-cancelled booking {} (deadline was {})", 
                        booking.getId(), booking.getPaymentDeadline());
            }
            
            if (cancelledCount > 0) {
                log.warn("Auto-cancelled {} unpaid booking(s)", cancelledCount);
            } else {
                log.info("No unpaid bookings to cancel");
            }
        } catch (Exception e) {
            log.error("Error during auto-cancellation", e);
        }
        
        return cancelledCount;
    }
    
    @Override
    public BookingResponseDto markAsDelivered(Integer bookingId, Integer travelerId) throws SendByOpException {
        log.info("Marking booking {} as delivered by traveler {}", bookingId, travelerId);
        
        // 1. Vérifier que la réservation existe
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Réservation non trouvée");
                });
        
        // 2. Vérifier que le voyageur est propriétaire du vol
        Integer flightOwnerId = booking.getFlight().getCustomer().getId();
        if (!flightOwnerId.equals(travelerId)) {
            log.error("Traveler {} is not owner of flight {}", travelerId, booking.getFlight().getFlightId());
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED,
                    "Vous n'êtes pas autorisé à marquer cette réservation comme livrée");
        }
        
        // 3. Vérifier que le statut est CONFIRMED_PAID
        if (booking.getStatus() != BookingStatus.CONFIRMED_PAID) {
            log.error("Invalid status for delivery: {}", booking.getStatus());
            throw new SendByOpException(ErrorInfo.INVALID_STATUS,
                    "La réservation doit être payée pour être marquée comme livrée");
        }
        
        // 4. Mettre à jour le statut vers DELIVERED
        booking.setStatus(BookingStatus.DELIVERED);
        
        // 5. Enregistrer la date de livraison
        booking.setDeliveredAt(LocalDateTime.now());
        
        // Sauvegarder
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} marked as delivered", bookingId);
        
        // Retourner la réponse
        return buildBookingResponse(saved);
    }
    
    @Override
    public BookingResponseDto markAsPickedUp(Integer bookingId, Integer customerId) throws SendByOpException {
        log.info("Marking booking {} as picked up by customer {}", bookingId, customerId);
        
        // 1. Vérifier que la réservation existe
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                            "Réservation non trouvée");
                });
        
        // 2. Vérifier que le client est propriétaire de la réservation
        if (!booking.getCustomer().getId().equals(customerId)) {
            log.error("Customer {} is not owner of booking {}", customerId, bookingId);
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED,
                    "Vous n'êtes pas autorisé à marquer cette réservation comme récupérée");
        }
        
        // 3. Vérifier que le statut est DELIVERED
        if (booking.getStatus() != BookingStatus.DELIVERED) {
            log.error("Invalid status for pickup: {}", booking.getStatus());
            throw new SendByOpException(ErrorInfo.INVALID_STATUS,
                    "La réservation doit être livrée pour être marquée comme récupérée");
        }
        
        // 4. Mettre à jour le statut vers PICKED_UP
        booking.setStatus(BookingStatus.PICKED_UP);
        
        // 5. Enregistrer la date de récupération
        booking.setPickedUpAt(LocalDateTime.now());
        
        // Sauvegarder
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} marked as picked up", bookingId);
        
        // Retourner la réponse
        return buildBookingResponse(saved);
    }
    
    /**
     * Construit un BookingResponseDto à partir d'une entité Booking
     */
    private BookingResponseDto buildBookingResponse(Booking booking) {
        Receiver receiver = booking.getReceiver();
        
        // Calculer poids total depuis les parcels
        Double totalWeight = booking.getParcels() != null ? 
                booking.getParcels().stream()
                        .mapToDouble(p -> p.getWeightKg() != null ? p.getWeightKg() : 0.0)
                        .sum() : 0.0;
        
        BookingResponseDto.BookingResponseDtoBuilder builder = BookingResponseDto.builder()
                .id(booking.getId())
                .status(booking.getStatus())
                .bookingDate(booking.getBookingDate())
                .confirmedAt(booking.getConfirmedAt())
                .paymentDeadline(booking.getPaymentDeadline())
                .totalPrice(booking.getTotalPrice())
                .flightId(booking.getFlight().getFlightId())
                .customerId(booking.getCustomer().getId())
                .parcelPhotoUrl(booking.getParcelPhotoUrl())
                .parcelWeight(BigDecimal.valueOf(totalWeight))
                .parcelDescription(booking.getParcels() != null && !booking.getParcels().isEmpty() ?
                        booking.getParcels().get(0).getDescription() : null);
        
        // Ajouter les infos du receiver seulement s'il existe
        if (receiver != null) {
            builder.receiverId(receiver.getId())
                    .receiverFullName(receiver.getFirstName() + " " + receiver.getLastName())
                    .receiverEmail(receiver.getEmail())
                    .receiverPhoneNumber(receiver.getPhoneNumber());
        } else {
            log.warn("Booking {} has no receiver associated", booking.getId());
        }
        
        return builder.build();
    }
    
    @Override
    public List<CustomerBookingDto> getCustomerBookingsByEmail(String email) throws SendByOpException {
        log.debug("Fetching bookings for customer email: {}", email);
        
        try {
            List<Booking> bookings = bookingRepository.findByCustomerEmailOrderByBookingDateDesc(email);
            
            List<CustomerBookingDto> customerBookings = bookings.stream()
                    .map(this::convertToCustomerBookingDto)
                    .collect(Collectors.toList());
            
            log.info("Successfully fetched {} bookings for customer: {}", customerBookings.size(), email);
            return customerBookings;
            
        } catch (Exception e) {
            log.error("Error fetching bookings for customer {}: {}", email, e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de la récupération des réservations");
        }
    }
    
    @Override
    public Page<CustomerBookingDto> getCustomerBookingsByEmailPaginated(String email, Pageable pageable) throws SendByOpException {
        log.debug("Fetching paginated bookings for customer email: {}, page: {}, size: {}", 
                email, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Booking> bookingsPage = bookingRepository.findByCustomerEmailOrderByBookingDateDesc(email, pageable);
            
            Page<CustomerBookingDto> customerBookingsPage = bookingsPage.map(this::convertToCustomerBookingDto);
            
            log.info("Successfully fetched {} bookings (page {} of {}) for customer: {}", 
                    customerBookingsPage.getNumberOfElements(),
                    pageable.getPageNumber(),
                    customerBookingsPage.getTotalPages(),
                    email);
            
            return customerBookingsPage;
            
        } catch (Exception e) {
            log.error("Error fetching paginated bookings for customer {}: {}", email, e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de la récupération des réservations");
        }
    }
    
    /**
     * Convertit une entité Booking en CustomerBookingDto
     */
    private CustomerBookingDto convertToCustomerBookingDto(Booking booking) {
        Flight flight = booking.getFlight();
        Receiver receiver = booking.getReceiver();
        
        // Construire FlightSummaryDto
        CustomerBookingDto.FlightSummaryDto flightSummary = CustomerBookingDto.FlightSummaryDto.builder()
                .id(flight.getFlightId())
                .departureDate(flight.getDepartureDate())
                .departureTime(flight.getDepartureTime())
                .arrivalDate(flight.getArrivalDate())
                .arrivalTime(flight.getArrivalTime())
                .availableWeight(flight.getKgCount() != null ? flight.getKgCount().doubleValue() : null)
                .pricePerKg(flight.getAmountPerKg() != null ? flight.getAmountPerKg().doubleValue() : null)
                .departureAirportName(flight.getDepartureAirport() != null ? flight.getDepartureAirport().getName() : null)
                .departureAirportCode(flight.getDepartureAirport() != null ? flight.getDepartureAirport().getIataCode() : null)
                .departureCityName(flight.getDepartureAirport() != null && flight.getDepartureAirport().getCity() != null ? 
                        flight.getDepartureAirport().getCity().getName() : null)
                .departureCountryName(flight.getDepartureAirport() != null && flight.getDepartureAirport().getCity() != null && 
                        flight.getDepartureAirport().getCity().getCountry() != null ? 
                        flight.getDepartureAirport().getCity().getCountry().getName() : null)
                .arrivalAirportName(flight.getArrivalAirport() != null ? flight.getArrivalAirport().getName() : null)
                .arrivalAirportCode(flight.getArrivalAirport() != null ? flight.getArrivalAirport().getIataCode() : null)
                .arrivalCityName(flight.getArrivalAirport() != null && flight.getArrivalAirport().getCity() != null ? 
                        flight.getArrivalAirport().getCity().getName() : null)
                .arrivalCountryName(flight.getArrivalAirport() != null && flight.getArrivalAirport().getCity() != null && 
                        flight.getArrivalAirport().getCity().getCountry() != null ? 
                        flight.getArrivalAirport().getCity().getCountry().getName() : null)
                .travelerFirstName(flight.getCustomer() != null ? flight.getCustomer().getFirstName() : null)
                .travelerLastName(flight.getCustomer() != null ? flight.getCustomer().getLastName() : null)
                .build();
        
        // Construire CustomerInfoDto (qui a fait la réservation)
        Customer customer = booking.getCustomer();
        CustomerBookingDto.CustomerInfoDto customerDto = null;
        if (customer != null) {
            customerDto = CustomerBookingDto.CustomerInfoDto.builder()
                    .id(customer.getId())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .email(customer.getEmail())
                    .phoneNumber(customer.getPhoneNumber())
                    .profilePictureUrl(customer.getProfilePicture() != null ? 
                            "/api/profile/picture/" + customer.getId() : null)
                    .build();
        }
        
        // Construire ReceiverDto
        CustomerBookingDto.ReceiverDto receiverDto = null;
        if (receiver != null) {
            receiverDto = CustomerBookingDto.ReceiverDto.builder()
                    .id(receiver.getId())
                    .firstName(receiver.getFirstName())
                    .lastName(receiver.getLastName())
                    .phoneNumber(receiver.getPhoneNumber())
                    .email(receiver.getEmail())
                    .address(receiver.getAddress())
                    .build();
        }
        
        // Construire liste de ParcelDto depuis les parcels de la réservation
        List<ParcelDto> parcelDtos = booking.getParcels() != null ?
                booking.getParcels().stream()
                        .map(parcel -> ParcelDto.builder()
                                .id(parcel.getId())
                                .description(parcel.getDescription())
                                .weightKg(parcel.getWeightKg())
                                .parcelType(parcel.getParcelType())
                                .build())
                        .collect(Collectors.toList()) :
                Collections.emptyList();
        
        // Calculer le poids total depuis les parcels
        Double totalWeight = booking.getParcels() != null ?
                booking.getParcels().stream()
                        .filter(p -> p.getWeightKg() != null)
                        .mapToDouble(Parcel::getWeightKg)
                        .sum() : 0.0;
        
        // Construire liste de ParcelPhotoDto
        List<ParcelPhotoDto> photoDtos = booking.getParcelPhotos() != null ?
                booking.getParcelPhotos().stream()
                        .map(photo -> ParcelPhotoDto.builder()
                                .id(photo.getId())
                                .photoUrl(photo.getPhotoUrl())
                                .description(photo.getDescription())
                                .displayOrder(photo.getDisplayOrder())
                                .isPrimary(photo.getIsPrimary())
                                .build())
                        .collect(Collectors.toList()) :
                Collections.emptyList();
        
        // Convertir le statut en valeurs numériques legacy
        int paymentStatus = convertStatusToPaymentStatus(booking.getStatus());
        int expeditionStatus = convertStatusToExpeditionStatus(booking.getStatus());
        int cancelled = isStatusCancelled(booking.getStatus()) ? 1 : 0;
        
        return CustomerBookingDto.builder()
                .id(booking.getId())
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingDate() != null ? 
                        new java.text.SimpleDateFormat("HH:mm").format(booking.getBookingDate()) : null)
                .paymentStatus(paymentStatus)
                .expeditionStatus(expeditionStatus)
                .cancelled(cancelled)
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .statusDisplayName(booking.getStatus() != null ? booking.getStatus().getDisplayName() : null)
                .customerReceptionStatus(booking.getStatus() == BookingStatus.PICKED_UP ? 1 : 0)
                .senderReceptionStatus(booking.getStatus() == BookingStatus.DELIVERED || 
                        booking.getStatus() == BookingStatus.PICKED_UP ? 1 : 0)
                .totalPrice(booking.getTotalPrice())
                .totalWeight(totalWeight)
                .flight(flightSummary)
                .customer(customerDto)
                .receiver(receiverDto)
                .parcels(parcelDtos)
                .parcelPhotos(photoDtos)
                .build();
    }
    
    /**
     * Convertit le BookingStatus en paymentStatus legacy (0: Pending, 1: Paid, 2: Failed)
     */
    private int convertStatusToPaymentStatus(BookingStatus status) {
        if (status == null) return 0;
        return switch (status) {
            case CONFIRMED_PAID, DELIVERED, PICKED_UP, IN_TRANSIT, CONFIRMED_BY_RECEIVER -> 1;
            case CANCELLED_PAYMENT_TIMEOUT -> 2;
            default -> 0;
        };
    }
    
    /**
     * Convertit le BookingStatus en expeditionStatus legacy (0: Pending, 1: In Progress, 2: Completed, 3: Delivered)
     */
    private int convertStatusToExpeditionStatus(BookingStatus status) {
        if (status == null) return 0;
        return switch (status) {
            case PENDING_CONFIRMATION, CONFIRMED_UNPAID -> 0;
            case CONFIRMED_PAID, IN_TRANSIT -> 1;
            case CONFIRMED_BY_RECEIVER -> 2;
            case DELIVERED, PICKED_UP -> 3;
            default -> 0;
        };
    }
    
    /**
     * Vérifie si le statut correspond à une annulation
     */
    private boolean isStatusCancelled(BookingStatus status) {
        if (status == null) return false;
        return status == BookingStatus.CANCELLED_BY_CLIENT ||
               status == BookingStatus.CANCELLED_BY_TRAVELER ||
               status == BookingStatus.CANCELLED_PAYMENT_TIMEOUT;
    }
    
    // ==========================================
    // MÉTHODES POUR LE VOYAGEUR (TRAVELER)
    // ==========================================
    
    @Override
    public List<CustomerBookingDto> getTravelerFlightBookings(String travelerEmail) throws SendByOpException {
        log.debug("Fetching bookings for traveler's flights: {}", travelerEmail);
        
        try {
            List<Booking> bookings = bookingRepository.findByFlightCustomerEmailOrderByBookingDateDesc(travelerEmail);
            
            List<CustomerBookingDto> bookingDtos = bookings.stream()
                    .map(this::convertToCustomerBookingDto)
                    .collect(Collectors.toList());
            
            log.info("Found {} bookings on flights for traveler: {}", bookingDtos.size(), travelerEmail);
            return bookingDtos;
            
        } catch (Exception e) {
            log.error("Error fetching traveler flight bookings for {}: {}", travelerEmail, e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de la récupération des réservations");
        }
    }
    
    @Override
    public Page<CustomerBookingDto> getTravelerFlightBookingsPaginated(String travelerEmail, Pageable pageable) throws SendByOpException {
        log.debug("Fetching paginated bookings for traveler's flights: {}, page: {}, size: {}", 
                travelerEmail, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Booking> bookingsPage = bookingRepository.findByFlightCustomerEmailOrderByBookingDateDesc(travelerEmail, pageable);
            
            Page<CustomerBookingDto> bookingDtosPage = bookingsPage.map(this::convertToCustomerBookingDto);
            
            log.info("Found {} bookings (page {} of {}) on flights for traveler: {}", 
                    bookingDtosPage.getNumberOfElements(),
                    pageable.getPageNumber(),
                    bookingDtosPage.getTotalPages(),
                    travelerEmail);
            
            return bookingDtosPage;
            
        } catch (Exception e) {
            log.error("Error fetching paginated traveler flight bookings for {}: {}", travelerEmail, e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, 
                    "Erreur lors de la récupération des réservations");
        }
    }
    
    @Override
    public CustomerBookingDto getBookingDetails(Integer bookingId, String requesterId) throws SendByOpException {
        log.debug("Fetching booking details: {} for user: {}", bookingId, requesterId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, "Réservation non trouvée");
                });
        
        // Vérifier que l'utilisateur est soit le client, soit le voyageur
        String customerEmail = booking.getCustomer().getEmail();
        String travelerEmail = booking.getFlight().getCustomer().getEmail();
        
        if (!requesterId.equals(customerEmail) && !requesterId.equals(travelerEmail)) {
            log.error("User {} is not authorized to view booking {}", requesterId, bookingId);
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED, 
                    "Vous n'êtes pas autorisé à voir cette réservation");
        }
        
        return convertToCustomerBookingDto(booking);
    }
}
