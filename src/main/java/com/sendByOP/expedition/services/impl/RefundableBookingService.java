package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.RefundableBooking;
import com.sendByOP.expedition.models.dto.RefundableBookingDto;
import com.sendByOP.expedition.mappers.RefundableBookingMapper;
import com.sendByOP.expedition.repositories.RefundableBookingRepository;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.services.iServices.IRefundableBookingService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.exception.ErrorInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RefundableBookingService implements IRefundableBookingService {

    private final RefundableBookingRepository refundableBookingRepository;
    private final RefundableBookingMapper refundableBookingMapper;
    private final ReservationService reservationService;

    @Override
    public RefundableBookingDto save(RefundableBookingDto refundableBookingDto) throws SendByOpException {
        log.debug("Saving refundable booking");
        try {
            RefundableBooking entity = refundableBookingMapper.toEntity(refundableBookingDto);
            RefundableBooking savedEntity = refundableBookingRepository.save(entity);
            log.info("Successfully saved refundable booking with ID: {}", savedEntity.getId());
            return refundableBookingMapper.toDto(savedEntity);
        } catch (Exception e) {
            log.error("Error saving refundable booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto findRefundableBooking(int id) throws SendByOpException {
        log.debug("Finding refundable booking with ID: {}", id);
        try {
            RefundableBookingDto refundableBooking = refundableBookingRepository.findById(id)
                    .map(refundableBookingMapper::toDto)
                    .orElseThrow(() -> {
                        log.error("Refundable booking not found with ID: {}", id);
                        return new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND);
                    });
            return reservationService.getBooking(refundableBooking.getBookingId());
        } catch (SendByOpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding refundable booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public List<RefundableBookingDto> findAll() throws SendByOpException {
        log.debug("Finding all refundable bookings");
        try {
            List<RefundableBookingDto> bookings = refundableBookingRepository.findAll()
                    .stream()
                    .map(refundableBookingMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Found {} refundable bookings", bookings.size());
            return bookings;
        } catch (Exception e) {
            log.error("Error finding all refundable bookings: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public void delete(int id) throws SendByOpException {
        log.debug("Deleting refundable booking with ID: {}", id);
        try {
            refundableBookingRepository.deleteById(id);
            log.info("Successfully deleted refundable booking with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting refundable booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }
}
