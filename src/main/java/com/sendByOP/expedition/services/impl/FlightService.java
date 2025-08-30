package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.AirportMapper;
import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.mappers.FlightMapper;
import com.sendByOP.expedition.models.dto.AirportDto;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.repositories.FlightRepository;
import com.sendByOP.expedition.services.iServices.IVolService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FlightService implements IVolService {
    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final StopoverService stopoverService;
    private final AirportService airportService;
    private final AirportMapper airportMapper;
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @Override
    public FlightDto getVolById(int id) {
        log.debug("Fetching flight with id: {}", id);
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
    public List<FlightDto> getAllVolValid(int status) {
        log.debug("Fetching all flights with validation status: {}", status);
        return flightRepository.findByValidationStatus(status).stream()
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public FlightDto saveVol(FlightDto flightDto) {
        log.debug("Saving flight: {}", flightDto);
        Flight flight = flightMapper.toEntity(flightDto);
        Flight savedFlight = flightRepository.save(flight);
        return flightMapper.toDto(savedFlight);
    }

    @Override
    public FlightDto saveVolWithEscales(VolEscaleDto flightWithStopoversDto) throws SendByOpException {
        log.debug("Saving flight with stopovers: {}", flightWithStopoversDto);
        try {
            Flight flightEntity = flightMapper.toEntity(flightWithStopoversDto.getVol());
            
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

            log.info("Successfully saved flight with stopovers, flight ID: {}", savedFlightDto.getFlightId());
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
}
