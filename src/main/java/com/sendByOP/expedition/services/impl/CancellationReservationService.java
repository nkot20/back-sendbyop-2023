package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CancellationReservationDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.entities.CancellationReservation;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.repositories.CancelReservationRepository;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.services.iServices.IAnnulationReservationService;
import com.sendByOP.expedition.mappers.CancellationReservationMapper;
import com.sendByOP.expedition.mappers.BookingMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationReservationService implements IAnnulationReservationService {

    private final CancelReservationRepository cancelReservationRepository;
    private final BookingRepository reservationRepository;
    private final CancellationReservationMapper cancellationMapper;
    private final BookingMapper bookingMapper;

    private final ReservationService reservationService;

    @Override
    @Transactional
    public CancellationReservationDto save(CancellationReservationDto annulationReservation) throws SendByOpException {
        try {
            log.info("Saving cancellation reservation");
            CancellationReservation entity = cancellationMapper.toEntity(annulationReservation);
            entity = cancelReservationRepository.save(entity);
            return cancellationMapper.toDto(entity);
        } catch (Exception e) {
            log.error("Error while saving cancellation reservation", e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Failed to save cancellation reservation");
        }
    }

    @Override
    @Transactional
    public BookingDto saveWithReservation(CancellationReservationDto annulationReservation) throws SendByOpException {
        try {
            log.info("Processing cancellation with booking update");
            
            // Save cancellation first
            CancellationReservation cancellation = cancellationMapper.toEntity(annulationReservation);
            cancelReservationRepository.save(cancellation);

            // Update booking status
            Booking booking = cancellation.getReservation();
            if (booking == null) {
                throw new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, "Booking not found for cancellation");
            }
            
            //TODO: booking.setS("CANCELLED");
            booking = reservationRepository.save(booking);
            
            return bookingMapper.toDto(booking);
        } catch (Exception e) {
            log.error("Error while processing cancellation with booking", e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Failed to process cancellation with booking");
        }
    }

    @Override
    public CancellationReservationDto findByReservation(int id) throws SendByOpException {
        try {
            log.info("Finding cancellation for reservation: {}", id);
            BookingDto bookingDto = reservationService.getBooking(id);
            Booking bookingEntity = bookingMapper.toEntity(bookingDto);
            return cancelReservationRepository.findByReservation(bookingEntity)
                .map(cancellationMapper::toDto)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                    "Cancellation not found for reservation: " + id));
        } catch (Exception e) {
            log.error("Error while finding cancellation for reservation: {}", id, e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Failed to find cancellation for reservation");
        }
    }

    @Override
    public void delete(CancellationReservationDto annulationReservation) throws SendByOpException {
        try {
            log.info("Deleting cancellation reservation");
            CancellationReservation entity = cancellationMapper.toEntity(annulationReservation);
            cancelReservationRepository.delete(entity);
        } catch (Exception e) {
            log.error("Error while deleting cancellation reservation", e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Failed to delete cancellation reservation");
        }
    }
}