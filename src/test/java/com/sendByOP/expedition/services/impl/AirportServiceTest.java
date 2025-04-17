package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.AirportMapper;
import com.sendByOP.expedition.models.dto.AirportDto;
import com.sendByOP.expedition.models.entities.Airport;
import com.sendByOP.expedition.models.entities.City;
import com.sendByOP.expedition.repositories.AirPortRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AirportServiceTest {

    @Mock
    private AirPortRepository airportRepository;

    @Mock
    private AirportMapper airportMapper;

    @InjectMocks
    private AirportService airportService;

    private Airport airport;
    private AirportDto airportDto;

    @BeforeEach
    void setUp() {
        // Setup test data using builder pattern
        airport = Airport.builder()
                .airportId(1)
                .name("Charles de Gaulle Airport")
                .city(City.builder().cityId(1).name("Paris").build())
                .iataCode("CDG")
                .build();

        airportDto = AirportDto.builder()
                .airportId(1)
                .name("Charles de Gaulle Airport")
                .cityId(1)
                .iataCode("CDG")
                .createdBy("system")
                .updatedBy("system")
                .build();
    }

    @Test
    void saveAeroPort_ShouldReturnSavedAirportDto() throws SendByOpException {
        // Arrange
        when(airportMapper.toEntity(any(AirportDto.class))).thenReturn(airport);
        when(airportRepository.save(any(Airport.class))).thenReturn(airport);
        when(airportMapper.toDto(any(Airport.class))).thenReturn(airportDto);

        // Act
        AirportDto result = airportService.saveAeroPort(airportDto);

        // Assert
        assertNotNull(result);
        assertEquals(airportDto.getName(), result.getName());
        assertEquals(airportDto.getAirportId(), result.getAirportId());
        assertEquals(airportDto.getIataCode(), result.getIataCode());
        verify(airportRepository, times(1)).save(any(Airport.class));
    }

    @Test
    void getAllAeroports_ShouldReturnListOfAirportDto() {
        // Arrange
        List<Airport> airports = Arrays.asList(airport);
        when(airportRepository.findAll()).thenReturn(airports);
        when(airportMapper.toDto(any(Airport.class))).thenReturn(airportDto);

        // Act
        List<AirportDto> result = airportService.getAllAirport();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(airportDto.getName(), result.get(0).getName());
        assertEquals(airportDto.getCityId(), result.get(0).getCityId());
        verify(airportRepository, times(1)).findAll();
    }

    @Test
    void getAirport_WithValidId_ShouldReturnAirportDto() throws SendByOpException {
        // Arrange
        when(airportRepository.findById(anyInt())).thenReturn(Optional.of(airport));
        when(airportMapper.toDto(any(Airport.class))).thenReturn(airportDto);

        // Act
        AirportDto result = airportService.getAirport(1);

        // Assert
        assertNotNull(result);
        assertEquals(airportDto.getName(), result.getName());
        assertEquals(airportDto.getAirportId(), result.getAirportId());
        assertEquals(airportDto.getIataCode(), result.getIataCode());
        verify(airportRepository, times(1)).findById(anyInt());
    }

    @Test
    void getAirport_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(airportRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        SendByOpException exception = assertThrows(SendByOpException.class, () -> {
            airportService.getAirport(999);
        });
        
        assertEquals(ErrorInfo.RESOURCE_NOT_FOUND, exception.getErrorInfo());
        verify(airportRepository, times(1)).findById(anyInt());
    }
}