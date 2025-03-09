package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.RejectionMapper;
import com.sendByOP.expedition.models.dto.RejectionDto;
import com.sendByOP.expedition.repositories.RefusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@Transactional
@RequiredArgsConstructor
public class RefusService {
    private final RefusRepository refusRepository;
    private final RejectionMapper refusMapper;

    public RejectionDto saveRefus(RejectionDto refus){
        return refusMapper.toDto(refusRepository.save(refusMapper.toEntity(refus)));
    }
}
