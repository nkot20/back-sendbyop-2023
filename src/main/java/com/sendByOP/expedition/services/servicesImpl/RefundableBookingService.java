package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.RefundableBooking;
import com.sendByOP.expedition.models.dto.RefundableBookingDto;
import com.sendByOP.expedition.mappers.RefundableBookingMapper;
import com.sendByOP.expedition.repositories.RefundableBookingRepository;
import com.sendByOP.expedition.services.iServices.IRefundableBookingService;
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

    @Override
    public RefundableBookingDto save(RefundableBookingDto refundableBookingDto) {
        RefundableBooking entity = refundableBookingMapper.toEntity(refundableBookingDto);
        RefundableBooking savedEntity = refundableBookingRepository.save(entity);
        return refundableBookingMapper.toDto(savedEntity);
    }

    @Override
    public List<RefundableBookingDto> findAll() {
        return refundableBookingRepository.findAll()
                .stream()
                .map(refundableBookingMapper::toDto)
                .collect(Collectors.toList());
    }

}
