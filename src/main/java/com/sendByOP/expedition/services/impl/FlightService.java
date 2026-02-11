package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.AirportMapper;
import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.mappers.FlightMapper;
import com.sendByOP.expedition.models.dto.AirportDto;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.PublicFlightDto;
import com.sendByOP.expedition.models.dto.PublicStopoverDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.models.entities.Review;
import com.sendByOP.expedition.models.entities.Stopover;
import com.sendByOP.expedition.models.enums.FlightStatus;
import com.sendByOP.expedition.repositories.FlightRepository;
import com.sendByOP.expedition.repositories.ParcelRepository;
import com.sendByOP.expedition.repositories.ReviewRepository;
import com.sendByOP.expedition.repositories.StopoverRepository;
import com.sendByOP.expedition.services.iServices.IVolService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FlightService implements IVolService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final ParcelRepository parcelRepository;
    private final StopoverRepository stopoverRepository;
    private final ReviewRepository reviewRepository;
    private final StopoverService stopoverService;
    private final AirportService airportService;
    private final AirportMapper airportMapper;
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final FraudGuardService fraudGuardService;
    private final SendMailService sendMailService;

    @Override
    @Cacheable(value = "flights", key = "#id")
    public FlightDto getVolById(int id) {
        log.debug("Fetching flight with id: {} from database (cache miss)", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with id: {}", id);
                    return new EntityNotFoundException("Flight not found with id: " + id);
                });
        return flightMapper.toDto(flight);
    }

    @Override
    public List<FlightDto> getAllVol() {
        log.debug("Fetching all flights ordered by departure date");
        List<Flight> flights = flightRepository.findAllByOrderByDepartureDateDesc();
        return flights.stream()
                .map(this::enrichFlightDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "flights:active", key = "#status")
    public List<FlightDto> getAllVolValid(int status) {
        log.debug("Fetching flights with status {} from database (cache miss)", status);
        return flightRepository.findByValidationStatus(status).stream()
                .map(this::enrichFlightDto)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"flights:active", "flights:public", "flights"}, allEntries = true)
    public FlightDto saveVol(FlightDto flightDto) {
        log.debug("Saving flight and invalidating cache: {}", flightDto);
        Flight flight = flightMapper.toEntity(flightDto);
        Flight savedFlight = flightRepository.save(flight);
        return flightMapper.toDto(savedFlight);
    }

    @Override
    @CacheEvict(value = {"flights:active", "flights:public", "flights"}, allEntries = true)
    public FlightDto saveVolWithEscales(VolEscaleDto flightWithStopoversDto) throws SendByOpException {
        log.info("Saving flight with stopovers and invalidating cache: {}", flightWithStopoversDto);
        try {
            // Récupération du customer connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Current authenticated user: {}", username);
            
            CustomerDto customerDto = customerService.getCustomerByEmail(username);
            
            // Validation KYC : vérifier que le voyageur est vérifié
            if (customerDto.getIdentityVerified() != 1) {
                log.warn("Customer {} attempted to publish flight without KYC verification", username);
                throw new SendByOpException(ErrorInfo.UNAUTHORIZED, 
                    "Votre identité doit être vérifiée avant de pouvoir publier un voyage. " +
                    "Veuillez soumettre vos documents d'identité dans votre profil.");
            }
            
            // Validation anti-fraude : vérifier la limite de voyages par semaine
            fraudGuardService.validateFlightLimit(null, username);
            
            Flight flightEntity = flightMapper.toEntity(flightWithStopoversDto.getVol());
            flightEntity.setCustomer(customerMapper.toEntity(customerDto));
            
            // Récupération et assignation des aéroports
            AirportDto departureAirportDto = airportService.getAirport(
                    flightWithStopoversDto.getVol().getDepartureAirportId()
            );
            AirportDto arrivalAirportDto = airportService.getAirport(
                    flightWithStopoversDto.getVol().getArrivalAirportId()
            );
            
            flightEntity.setDepartureAirport(airportMapper.toEntity(departureAirportDto));
            flightEntity.setArrivalAirport(airportMapper.toEntity(arrivalAirportDto));

            // Définir le statut initial à PENDING_VALIDATION
            flightEntity.setStatus(FlightStatus.PENDING_VALIDATION);
            
            // Sauvegarde directe de l'entité avec les aéroports assignés
            Flight savedFlight = flightRepository.save(flightEntity);
            FlightDto savedFlightDto = flightMapper.toDto(savedFlight);
            
            // Envoyer un email de confirmation au voyageur
            sendFlightCreationEmail(savedFlight);

            // Traitement des escales si présentes
            if (flightWithStopoversDto.getEscales() != null && !flightWithStopoversDto.getEscales().isEmpty()) {
                flightWithStopoversDto.getEscales().forEach(stopoverDto -> {
                    stopoverDto.setFlightId(savedFlight.getFlightId());
                    stopoverService.addStopover(stopoverDto);
                });
            }

            log.info("Successfully saved flight with stopovers, flight ID: {} for customer: {}", savedFlightDto.getFlightId(), username);
            return savedFlightDto;
        } catch (Exception e) {
            log.error("Error saving flight with stopovers", e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }


    @Override
    public void deleteVol(int id) throws SendByOpException {
        log.debug("Deleting flight with id: {}", id);
        try {
            flightRepository.deleteById(id);
            log.info("Successfully deleted flight with id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting flight with id: {}", id, e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public FlightDto updateVol(FlightDto flightDto) throws SendByOpException {
        log.debug("Updating flight: {}", flightDto);
        try {
            Flight flight = flightMapper.toEntity(flightDto);
            Flight updatedFlight = flightRepository.save(flight);
            log.info("Successfully updated flight with id: {}", flightDto.getFlightId());
            return flightMapper.toDto(updatedFlight);
        } catch (Exception e) {
            log.error("Error updating flight", e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public List<FlightDto> getByIdClient(CustomerDto customer) throws SendByOpException {
        log.debug("Fetching flights for customer with id: {}", customer.getEmail());
        CustomerDto customerDto = customerService.getCustomerByEmail(customer.getEmail());
        List<Flight> flights = flightRepository
                .findByCustomerOrderByPublicationDateDesc(customerMapper.toEntity(customerDto));
        return flights.stream()
                .map(this::enrichFlightDto)
                .collect(Collectors.toList());
    }

    @Override
    public int nbVolClient(CustomerDto customer) throws SendByOpException {
        log.debug("Counting validated flights for customer with id: {}", customer.getId());
        CustomerDto customerDto = customerService.getCustomerByEmail(customer.getEmail());
        List<Flight> flights = flightRepository
                .findByCustomerOrderByPublicationDateDesc(customerMapper.toEntity(customerDto));
        flights.removeIf(flight -> flight.getValidationStatus() != 1);
        return flights.size();
    }

    @Override
    public FlightDto getVolByIdVol(int id) {
        log.debug("Fetching flight by flight id: {}", id);
        Flight flight = flightRepository.findByFlightId(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with flight id: {}", id);
                    return new EntityNotFoundException("Flight not found with flight id: " + id);
                });
        return flightMapper.toDto(flight);
    }

    @Override
    public List<FlightDto> getValidAndActiveFlights() {
        log.debug("Fetching valid and active flights");
        Date currentDate = new Date();
        List<Flight> flights = flightRepository.findByValidationStatusAndCancelledOrderByDepartureDateDesc(1, 0);
        
        // Filter flights with departure date in the future
        List<Flight> activeFuture = flights.stream()
                .filter(flight -> flight.getDepartureDate().after(currentDate))
                .collect(Collectors.toList());
        
        log.info("Found {} valid and active flights", activeFuture.size());
        return activeFuture.stream()
                .map(this::enrichFlightDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PublicFlightDto> getPublicValidAndActiveFlights() {
        log.debug("Fetching public valid and active flights with detailed information");
        Date currentDate = new Date();
        List<Flight> flights = flightRepository.findByValidationStatusAndCancelledOrderByDepartureDateDesc(1, 0);
        
        // Filter flights with departure date in the future AND status ACTIVE (not EXPIRED or CANCELLED)
        List<Flight> activeFuture = flights.stream()
                .filter(flight -> flight.getDepartureDate().after(currentDate))
                .filter(flight -> flight.getStatus() == FlightStatus.ACTIVE)
                .collect(Collectors.toList());
        
        log.info("Found {} valid and active flights for public API", activeFuture.size());
        
        return activeFuture.stream()
                .map(this::convertToPublicFlightDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PublicFlightDto> getPublicValidAndActiveFlights(Pageable pageable) {
        log.debug("Fetching paginated public valid and active flights with detailed information");
        
        // Get paginated flights from repository
        Page<Flight> flightsPage = flightRepository.findByValidationStatusAndCancelledAndDepartureDateAfterOrderByDepartureDateDesc(1, 0, pageable);
        
        // Convert to DTO directly (filtering is done at repository level for better performance)
        Page<PublicFlightDto> publicFlightsPage = flightsPage.map(this::convertToPublicFlightDto);
        
        log.info("Found {} valid and active flights for public API (page {} of {})", 
                publicFlightsPage.getNumberOfElements(), 
                publicFlightsPage.getNumber() + 1, 
                publicFlightsPage.getTotalPages());
        
        return publicFlightsPage;
    }

    @Override
    public PublicFlightDto getPublicFlightById(int id) {
        log.debug("Fetching public flight details for flight id: {}", id);
        Flight flight = flightRepository.findByFlightId(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with flight id: {}", id);
                    return new EntityNotFoundException("Flight not found with flight id: " + id);
                });
        
        // Vérifier que le vol n'est pas expiré
        if (flight.getStatus() == FlightStatus.EXPIRED) {
            log.warn("Attempt to access expired flight: {}", id);
            throw new EntityNotFoundException("Ce vol n'est plus disponible (expiré)");
        }
        
        PublicFlightDto publicFlightDto = convertToPublicFlightDto(flight);
        log.info("Successfully retrieved public flight details for flight id: {}", id);
        return publicFlightDto;
    }

    private PublicFlightDto convertToPublicFlightDto(Flight flight) {
        // Calculate available weight
        Float totalBookedWeight = parcelRepository.getTotalWeightByFlightId(flight.getFlightId());
        if (totalBookedWeight == null) {
            totalBookedWeight = 0.0f;
        } 
        float availableWeight = flight.getKgCount() - totalBookedWeight;
        
        // Get stopovers for this flight
        List<Stopover> stopovers = stopoverRepository.findByFlight(flight);
        List<PublicStopoverDto> stopoverDtos = stopovers.stream()
                .map(this::convertToPublicStopoverDto)
                .collect(Collectors.toList());
        
        // Calculate traveler average rating and review count
        List<Review> travelerReviews = reviewRepository.findByTransporterIdAndBookingIsNotNull(flight.getCustomer().getId());
        Double averageRating = null;
        Integer reviewCount = travelerReviews.size();
        
        if (!travelerReviews.isEmpty()) {
            double sum = travelerReviews.stream()
                    .mapToDouble(review -> {
                        try {
                            // Convertir String rating en double
                            return review.getRating() != null ? Double.parseDouble(review.getRating()) : 0.0;
                        } catch (NumberFormatException e) {
                            log.warn("Invalid rating format for review id {}: {}", review.getId(), review.getRating());
                            return 0.0;
                        }
                    })
                    .sum();
            averageRating = sum / travelerReviews.size();
            // Arrondir à 1 décimale
            averageRating = Math.round(averageRating * 10.0) / 10.0;
        }
        
        // Get traveler profile picture URL
        String profilePictureUrl = null;
        if (flight.getCustomer().getProfilePicture() != null && !flight.getCustomer().getProfilePicture().isEmpty()) {
            profilePictureUrl = "/uploads/profile-pictures/" + flight.getCustomer().getProfilePicture();
        }
        
        return PublicFlightDto.builder()
                .flightId(flight.getFlightId())
                .departureDate(flight.getDepartureDate())
                .departureTime(flight.getDepartureTime())
                .arrivalDate(flight.getArrivalDate())
                .arrivalTime(flight.getArrivalTime())
                .amountPerKg(flight.getAmountPerKg())
                .kgCount(flight.getKgCount())
                .availableKg((int) availableWeight)
                .preference(flight.getPreference())
                .publicationDate(flight.getPublicationDate())
                .image(flight.getImage())
                .depositLocation(flight.getDepositLocation())
                .receptionLocation(flight.getReceptionLocation())
                .departureAirportName(flight.getDepartureAirport().getName())
                .departureAirportCode(flight.getDepartureAirport().getIataCode())
                .departureCityName(flight.getDepartureAirport().getCity().getName())
                .departureCountryName(flight.getDepartureAirport().getCity().getCountry().getName())
                .arrivalAirportName(flight.getArrivalAirport().getName())
                .arrivalAirportCode(flight.getArrivalAirport().getIataCode())
                .arrivalCityName(flight.getArrivalAirport().getCity().getName())
                .arrivalCountryName(flight.getArrivalAirport().getCity().getCountry().getName())
                .customerFirstName(flight.getCustomer().getFirstName())
                .customerLastName(flight.getCustomer().getLastName())
                .travelerProfilePictureUrl(profilePictureUrl)
                .travelerAverageRating(averageRating)
                .travelerReviewCount(reviewCount)
                .stopovers(stopoverDtos)
                .build();
    }

    private PublicStopoverDto convertToPublicStopoverDto(Stopover stopover) {
        return PublicStopoverDto.builder()
                .id(stopover.getId())
                .date(stopover.getDate())
                .hour(stopover.getHour())
                .airportName(stopover.getAirport().getName())
                .airportCode(stopover.getAirport().getIataCode())
                .cityName(stopover.getAirport().getCity().getName())
                .countryName(stopover.getAirport().getCity().getCountry().getName())
                .build();
    }
    
    /**
     * Valide un vol en attente - change le statut à ACTIVE
     * Envoie un email de notification au voyageur
     */
    public FlightDto validateFlight(int flightId) throws SendByOpException {
        log.info("Validation du vol ID: {}", flightId);
        
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                    "Vol non trouvé avec l'ID: " + flightId));
        
        if (flight.getStatus() != FlightStatus.PENDING_VALIDATION) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                "Ce vol n'est pas en attente de validation. Statut actuel: " + flight.getStatus().getDisplayName());
        }
        
        // Changer le statut à ACTIVE
        flight.setStatus(FlightStatus.ACTIVE);
        flight.setValidationStatus(1); // Pour compatibilité avec l'ancien système
        Flight validatedFlight = flightRepository.save(flight);
        
        // Envoyer un email de validation au voyageur
        sendFlightValidationEmail(validatedFlight);
        
        log.info("Vol {} validé avec succès", flightId);
        return flightMapper.toDto(validatedFlight);
    }
    
    /**
     * Rejette un vol en attente - change le statut à REJECTED
     * Envoie un email de notification au voyageur avec la raison du rejet
     */
    public FlightDto rejectFlight(int flightId, String reason) throws SendByOpException {
        log.info("Rejet du vol ID: {} - Raison: {}", flightId, reason);
        
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, 
                    "Vol non trouvé avec l'ID: " + flightId));
        
        if (flight.getStatus() != FlightStatus.PENDING_VALIDATION) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA,
                "Ce vol n'est pas en attente de validation. Statut actuel: " + flight.getStatus().getDisplayName());
        }
        
        // Changer le statut à REJECTED
        flight.setStatus(FlightStatus.REJECTED);
        flight.setValidationStatus(0); // Pour compatibilité avec l'ancien système
        Flight rejectedFlight = flightRepository.save(flight);
        
        // Envoyer un email de rejet au voyageur
        sendFlightRejectionEmail(rejectedFlight, reason);
        
        log.info("Vol {} rejeté avec succès", flightId);
        return flightMapper.toDto(rejectedFlight);
    }
    
    /**
     * Récupère tous les vols en attente de validation avec informations enrichies
     */
    public List<FlightDto> getPendingFlights() {
        log.debug("Récupération des vols en attente de validation");
        List<Flight> pendingFlights = flightRepository.findByStatusOrderByPublicationDateDesc(FlightStatus.PENDING_VALIDATION);
        log.info("Trouvé {} vols en attente de validation", pendingFlights.size());
        
        return pendingFlights.stream()
                .map(this::enrichFlightDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Envoie un email au voyageur après la création de son vol
     */
    private void sendFlightCreationEmail(Flight flight) {
        try {
            String toEmail = flight.getCustomer().getEmail();
            String subject = "Votre vol a été publié avec succès";
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #4CAF50;">Vol publié avec succès !</h2>
                        <p>Bonjour %s,</p>
                        <p>Votre vol a été publié avec succès sur SendByOp.</p>
                        
                        <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h3 style="margin-top: 0;">Détails du vol :</h3>
                            <p><strong>Départ :</strong> %s (%s) - %s</p>
                            <p><strong>Arrivée :</strong> %s (%s) - %s</p>
                            <p><strong>Capacité :</strong> %d kg</p>
                            <p><strong>Prix :</strong> %d € / kg</p>
                        </div>
                        
                        <div style="background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0;">
                            <p style="margin: 0;"><strong>⏳ En attente de validation</strong></p>
                            <p style="margin: 10px 0 0 0;">Votre vol doit être validé par notre équipe avant d'être visible aux clients. Nous vérifierons votre billet d'avion et validerons votre vol dans les plus brefs délais.</p>
                        </div>
                        
                        <p>Vous recevrez un email dès que votre vol sera validé.</p>
                        
                        <p>Cordialement,<br>L'équipe SendByOp</p>
                    </div>
                </body>
                </html>
                """,
                flight.getCustomer().getFirstName(),
                flight.getDepartureAirport().getCity().getName(),
                flight.getDepartureAirport().getIataCode(),
                flight.getDepartureDate(),
                flight.getArrivalAirport().getCity().getName(),
                flight.getArrivalAirport().getIataCode(),
                flight.getArrivalDate(),
                flight.getKgCount(),
                flight.getAmountPerKg()
            );
            
            sendMailService.sendHtmlEmail(toEmail, subject, htmlContent);
            log.info("Email de création de vol envoyé à {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de création de vol", e);
        }
    }
    
    /**
     * Envoie un email au voyageur après validation de son vol
     */
    private void sendFlightValidationEmail(Flight flight) {
        try {
            String toEmail = flight.getCustomer().getEmail();
            String subject = "✅ Votre vol a été validé !";
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #4CAF50;">✅ Vol validé avec succès !</h2>
                        <p>Bonjour %s,</p>
                        <p>Bonne nouvelle ! Votre vol a été validé par notre équipe et est maintenant visible aux clients.</p>
                        
                        <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h3 style="margin-top: 0;">Détails du vol :</h3>
                            <p><strong>Départ :</strong> %s (%s) - %s</p>
                            <p><strong>Arrivée :</strong> %s (%s) - %s</p>
                            <p><strong>Capacité :</strong> %d kg</p>
                            <p><strong>Prix :</strong> %d € / kg</p>
                        </div>
                        
                        <div style="background-color: #d4edda; padding: 15px; border-left: 4px solid #28a745; margin: 20px 0;">
                            <p style="margin: 0;"><strong>🎉 Votre vol est maintenant actif !</strong></p>
                            <p style="margin: 10px 0 0 0;">Les clients peuvent désormais réserver de l'espace sur votre vol. Vous recevrez une notification pour chaque nouvelle réservation.</p>
                        </div>
                        
                        <p>Merci de votre confiance et bon voyage !</p>
                        
                        <p>Cordialement,<br>L'équipe SendByOp</p>
                    </div>
                </body>
                </html>
                """,
                flight.getCustomer().getFirstName(),
                flight.getDepartureAirport().getCity().getName(),
                flight.getDepartureAirport().getIataCode(),
                flight.getDepartureDate(),
                flight.getArrivalAirport().getCity().getName(),
                flight.getArrivalAirport().getIataCode(),
                flight.getArrivalDate(),
                flight.getKgCount(),
                flight.getAmountPerKg()
            );
            
            sendMailService.sendHtmlEmail(toEmail, subject, htmlContent);
            log.info("Email de validation de vol envoyé à {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de validation de vol", e);
        }
    }
    
    /**
     * Envoie un email au voyageur après rejet de son vol
     */
    private void sendFlightRejectionEmail(Flight flight, String reason) {
        try {
            String toEmail = flight.getCustomer().getEmail();
            String subject = "❌ Votre vol n'a pas été validé";
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #dc3545;">Vol non validé</h2>
                        <p>Bonjour %s,</p>
                        <p>Nous regrettons de vous informer que votre vol n'a pas pu être validé.</p>
                        
                        <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h3 style="margin-top: 0;">Détails du vol :</h3>
                            <p><strong>Départ :</strong> %s (%s) - %s</p>
                            <p><strong>Arrivée :</strong> %s (%s) - %s</p>
                        </div>
                        
                        <div style="background-color: #f8d7da; padding: 15px; border-left: 4px solid #dc3545; margin: 20px 0;">
                            <p style="margin: 0;"><strong>Raison du rejet :</strong></p>
                            <p style="margin: 10px 0 0 0;">%s</p>
                        </div>
                        
                        <p>Si vous pensez qu'il s'agit d'une erreur ou si vous avez des questions, n'hésitez pas à nous contacter.</p>
                        
                        <p>Cordialement,<br>L'équipe SendByOp</p>
                    </div>
                </body>
                </html>
                """,
                flight.getCustomer().getFirstName(),
                flight.getDepartureAirport().getCity().getName(),
                flight.getDepartureAirport().getIataCode(),
                flight.getDepartureDate(),
                flight.getArrivalAirport().getCity().getName(),
                flight.getArrivalAirport().getIataCode(),
                flight.getArrivalDate(),
                reason != null ? reason : "Informations du billet d'avion non conformes ou incomplètes."
            );
            
            sendMailService.sendHtmlEmail(toEmail, subject, htmlContent);
            log.info("Email de rejet de vol envoyé à {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de rejet de vol", e);
        }
    }
    
    /**
     * Méthode helper pour enrichir un FlightDto avec toutes les informations nécessaires
     * (aéroports, villes, pays, client, statut)
     */
    private FlightDto enrichFlightDto(Flight flight) {
        FlightDto dto = flightMapper.toDto(flight);
        
        // Enrichir avec les informations de l'aéroport de départ
        if (flight.getDepartureAirport() != null) {
            dto.setDepartureAirportName(flight.getDepartureAirport().getName());
            dto.setDepartureAirportCode(flight.getDepartureAirport().getIataCode());
            if (flight.getDepartureAirport().getCity() != null) {
                dto.setDepartureCityName(flight.getDepartureAirport().getCity().getName());
                if (flight.getDepartureAirport().getCity().getCountry() != null) {
                    dto.setDepartureCountryName(flight.getDepartureAirport().getCity().getCountry().getName());
                }
            }
        }
        
        // Enrichir avec les informations de l'aéroport d'arrivée
        if (flight.getArrivalAirport() != null) {
            dto.setArrivalAirportName(flight.getArrivalAirport().getName());
            dto.setArrivalAirportCode(flight.getArrivalAirport().getIataCode());
            if (flight.getArrivalAirport().getCity() != null) {
                dto.setArrivalCityName(flight.getArrivalAirport().getCity().getName());
                if (flight.getArrivalAirport().getCity().getCountry() != null) {
                    dto.setArrivalCountryName(flight.getArrivalAirport().getCity().getCountry().getName());
                }
            }
        }
        
        // Enrichir avec les informations du client
        if (flight.getCustomer() != null) {
            dto.setCustomerFirstName(flight.getCustomer().getFirstName());
            dto.setCustomerLastName(flight.getCustomer().getLastName());
            dto.setCustomerEmail(flight.getCustomer().getEmail());
        }
        
        // Ajouter le statut sous forme de string
        if (flight.getStatus() != null) {
            dto.setStatus(flight.getStatus().name());
        }
        
        return dto;
    }
}
