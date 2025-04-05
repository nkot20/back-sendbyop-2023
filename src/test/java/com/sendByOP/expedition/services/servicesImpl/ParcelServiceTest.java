package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.ParcelMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.ParcelDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.entities.Parcel;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.repositories.ParcelRepository;
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
public class ParcelServiceTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapper parcelMapper;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private ParcelService parcelService;

    private Parcel parcel;
    private ParcelDto parcelDto;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        // Setup test data using builder pattern
        booking = Booking.builder()
                .id(1)
                .build();

        bookingDto = BookingDto.builder()
                .id(1)
                .build();

        parcel = Parcel.builder()
                .id(1)
                .description("Test Parcel")
                .reservation(booking)
                .build();

        parcelDto = ParcelDto.builder()
                .id(1)
                .description("Test Parcel")
                .reservationId(bookingDto.getId())
                .build();
    }

    @Test
    void saveParcel_ShouldReturnSavedParcelDto() throws SendByOpException {
        // Arrange
        when(parcelMapper.toEntity(any(ParcelDto.class))).thenReturn(parcel);
        when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);
        when(parcelMapper.toDto(any(Parcel.class))).thenReturn(parcelDto);

        // Act
        ParcelDto result = parcelService.saveParcel(parcelDto);

        // Assert
        assertNotNull(result);
        assertEquals(parcelDto.getDescription(), result.getDescription());
        assertEquals(parcelDto.getId(), result.getId());
        verify(parcelRepository, times(1)).save(any(Parcel.class));
    }

    @Test
    void saveParcel_WithNullDescription_ShouldThrowException() {
        // Arrange
        ParcelDto invalidParcel = ParcelDto.builder()
                .id(1)
                .description(null)
                .build();

        // Act & Assert
        assertThrows(SendByOpException.class, () -> parcelService.saveParcel(invalidParcel));
        verify(parcelRepository, never()).save(any(Parcel.class));
    }

    @Test
    void deleteParcel_ShouldCallRepositoryDelete() {
        // Act
        parcelService.deleteParcel(parcelDto);

        // Assert
        verify(parcelMapper, times(1)).toEntity(parcelDto);
        verify(parcelRepository, times(1)).delete(any(Parcel.class));
    }

    @Test
    void findParcelById_WithValidId_ShouldReturnParcelDto() throws SendByOpException {
        // Arrange
        when(parcelRepository.findById(anyInt())).thenReturn(Optional.of(parcel));
        when(parcelMapper.toDto(any(Parcel.class))).thenReturn(parcelDto);

        // Act
        ParcelDto result = parcelService.findParcelById(1);

        // Assert
        assertNotNull(result);
        assertEquals(parcelDto.getId(), result.getId());
        assertEquals(parcelDto.getDescription(), result.getDescription());
        verify(parcelRepository, times(1)).findById(1);
    }

    @Test
    void findParcelById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(parcelRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        SendByOpException exception = assertThrows(SendByOpException.class,
                () -> parcelService.findParcelById(999));
        assertEquals(ErrorInfo.RESSOURCE_NOT_FOUND, exception.getErrorInfo());
    }

    @Test
    void findAllParcelsByBooking_ShouldReturnListOfParcels() {
        // Arrange
        List<Parcel> parcels = Arrays.asList(parcel);
        when(bookingMapper.toEntity(any(BookingDto.class))).thenReturn(booking);
        when(parcelRepository.findByIdre(any(Booking.class))).thenReturn(parcels);
        when(parcelMapper.toDtoList(anyList())).thenReturn(Arrays.asList(parcelDto));

        // Act
        List<ParcelDto> results = parcelService.findAllParcelsByBooking(bookingDto);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(parcelDto.getId(), results.get(0).getId());
        assertEquals(parcelDto.getDescription(), results.get(0).getDescription());
        verify(parcelRepository, times(1)).findByIdre(any(Booking.class));
    }
}