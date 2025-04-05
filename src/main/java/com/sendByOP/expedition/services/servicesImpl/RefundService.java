package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.RefundMapper;
import com.sendByOP.expedition.models.dto.RefundDto;
import com.sendByOP.expedition.models.entities.Refund;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.repositories.RefundRepository;
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
public class RefundService {

    private final RefundRepository refundRepository;
    private final RefundMapper refundMapper;

    public RefundDto save(RefundDto refundDto) throws SendByOpException {
        if (refundDto == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
        }
        try {
            Refund refund = refundMapper.toEntity(refundDto);
            Refund savedRefund = refundRepository.save(refund);
            log.info("Successfully saved refund with ID: {}", savedRefund.getId());
            return refundMapper.toDto(savedRefund);
        } catch (Exception e) {
            log.error("Error saving refund: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

    public List<RefundDto> getRefunds() {
        List<Refund> refunds = refundRepository.findAll();
        log.info("Retrieved {} refunds", refunds.size());
        return refunds.stream()
                .map(refundMapper::toDto)
                .collect(Collectors.toList());
    }

    public RefundDto findByReservation(Booking reservation) throws SendByOpException {
        if (reservation == null) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
        }
        try {
            Refund refund = refundRepository.findByReservation(reservation)
                    .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
            log.info("Found refund for reservation ID: {}", reservation.getId());
            return refundMapper.toDto(refund);
        } catch (SendByOpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding refund for reservation: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

}
