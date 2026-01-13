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
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "flights:active", key = "#status")
    public List<FlightDto> getAllVolValid(int status) {
        log.debug("Fetching flights with status {} from database (cache miss)", status);
        return flightRepository.findByValidationStatus(status).stream()
                .map(flightMapper::toDto)
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
            Flight flightEntity = flightMapper.toEntity(flightWithStopoversDto.getVol());
            
            // Récupération du customer connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Current authenticated user: {}", username);
            
            CustomerDto customerDto = customerService.getCustomerByEmail(username);
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

            // Sauvegarde directe de l'entité avec les aéroports assignés
            Flight savedFlight = flightRepository.save(flightEntity);
            FlightDto savedFlightDto = flightMapper.toDto(savedFlight);

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
                .map(flightMapper::toDto)
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
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PublicFlightDto> getPublicValidAndActiveFlights() {
        log.debug("Fetching public valid and active flights with detailed information");
        Date currentDate = new Date();
        List<Flight> flights = flightRepository.findByValidationStatusAndCancelledOrderByDepartureDateDesc(1, 0);
        
        // Filter flights with departure date in the future
        List<Flight> activeFuture = flights.stream()
                .filter(flight -> flight.getDepartureDate().after(currentDate))
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
}
