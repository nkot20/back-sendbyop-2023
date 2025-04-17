package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.mappers.RejectionMapper;
import com.sendByOP.expedition.models.dto.RejectionDto;
import com.sendByOP.expedition.repositories.RejectionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@Transactional
@RequiredArgsConstructor
public class RejectionService {
    private final RejectionRepository refusRepository;
    private final RejectionMapper rejectionMapper;

    public RejectionDto saveRejection(RejectionDto rejectionDto){
        return rejectionMapper.toDto(refusRepository.save(rejectionMapper.toEntity(rejectionDto)));
    }
}
