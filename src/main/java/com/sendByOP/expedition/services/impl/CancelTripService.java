package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.mappers.CancellationTripMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.mappers.FlightMapper;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.repositories.CancelTripRepository;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.exception.ErrorInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CancelTripService {

    private final CancelTripRepository cancelTripRepository;
    private final ReservationService reservationService;
    private final FlightService volService;
    private final FlightMapper volMapper;
    private final CancellationTripMapper cancellationTripMapper;
    private final SendMailService sendMailService;
    private final RefundableBookingService refundableBookingService;
    private final BookingMapper reservationMapper;

    public CancellationTripDto save(CancellationTripDto cancellationTripDto) throws SendByOpException {
        try {
            if (cancellationTripDto == null) {
                throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
            }
            CancellationTrip cancellationTrip = CancellationTrip.builder()
                .cancellationId(cancellationTripDto.getCancellationId())
                .reason(cancellationTripDto.getReason())
                .cancellationDate(new Date())
                .trip(volMapper.toEntity(volService.getVolById(cancellationTripDto.getTripId())))
                .viewed(cancellationTripDto.getViewed())
                .build();
            CancellationTrip savedCancellationTrip = cancelTripRepository.save(cancellationTrip);
            log.info("Successfully saved cancellation request for trip {}", cancellationTripDto.getTripId());
            return CancellationTripDto.builder()
                .cancellationId(savedCancellationTrip.getCancellationId())
                .reason(savedCancellationTrip.getReason())
                .cancellationDate(savedCancellationTrip.getCancellationDate())
                .tripId(savedCancellationTrip.getTrip().getFlightId())
                .viewed(savedCancellationTrip.getViewed())
                .build();
        } catch (Exception e) {
            log.error("Error saving cancellation request: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    public FlightDto cancelTrip(CancellationTripDto cancellationTripDto) throws SendByOpException {
        if (cancellationTripDto == null || cancellationTripDto.getTripId() == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }

        try {
            CancellationTripDto savedCancellationTrip = save(cancellationTripDto);

            Integer flightId = cancellationTripDto.getTripId();
            FlightDto flightDto = volService.getVolById(flightId);
            if (flightDto == null) {
                throw new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND);
            }

            flightDto.setCancelled(1);
            FlightDto updatedFlight = volService.updateVol(flightDto);
            if (updatedFlight == null) {
                delete(savedCancellationTrip);
                throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
            }

            List<BookingDto> bookings = reservationService.getAllBookings();
            bookings.removeIf(booking ->
                    !Objects.equals(booking.getFlightId(), updatedFlight.getFlightId()) ||
                            booking.getPaymentStatus() == 1
            );

            List<EmailDto> emails = new ArrayList<>();
            log.info("Processing cancellation for flight {}", flightId);
            for (BookingDto booking : bookings) {
                RefundableBookingDto refundableBooking = RefundableBookingDto.builder()
                    .bookingId(reservationMapper.toEntity(booking).getId())
                    .validated(0)
                    .build();
                refundableBookingService.save(refundableBooking);

                EmailDto email = EmailDto.builder()
                    .body(String.format(
                        "La réservation que vous avez effectuée le %s a été annulée par l'expéditeur pour cause de %s. Un remboursement vous sera effectué.",
                        booking.getBookingDate(),
                        cancellationTripDto.getReason()
                    ))
                    .build();
                emails.add(email);
            }

            sendMailService.sendListEmail(emails);
            return updatedFlight;
        } catch (Exception e) {
            log.error("Error processing trip cancellation: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    public void delete(CancellationTripDto cancellationTripDto) throws SendByOpException {
        if (cancellationTripDto == null || cancellationTripDto.getCancellationId() == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
        try {
            CancellationTrip cancellationTrip = cancellationTripMapper.toEntity(cancellationTripDto);
            cancelTripRepository.delete(cancellationTrip);
            log.info("Successfully deleted cancellation request with ID {}", cancellationTripDto.getCancellationId());
        } catch (Exception e) {
            log.error("Error deleting cancellation request: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    public CancellationTripDto findByFlight(FlightDto flightDto) throws SendByOpException {
        if (flightDto == null || flightDto.getFlightId() == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
        try {
            Flight flight = volMapper.toEntity(flightDto);
            CancellationTrip cancellationTrip = cancelTripRepository.findByTrip(flight)
                    .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
            log.info("Found cancellation request for flight {}", flightDto.getFlightId());
            return cancellationTripMapper.toDto(cancellationTrip);
        } catch (SendByOpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding cancellation request: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    public CancellationTripDto update(CancellationTripDto cancellationTripDto) throws SendByOpException {
        if (cancellationTripDto == null || cancellationTripDto.getCancellationId() == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
        try {
            CancellationTrip cancellationTrip = cancellationTripMapper.toEntity(cancellationTripDto);
            CancellationTrip updatedCancellationTrip = cancelTripRepository.save(cancellationTrip);
            log.info("Successfully updated cancellation request with ID {}", cancellationTripDto.getCancellationId());
            return cancellationTripMapper.toDto(updatedCancellationTrip);
        } catch (Exception e) {
            log.error("Error updating cancellation request: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }
}