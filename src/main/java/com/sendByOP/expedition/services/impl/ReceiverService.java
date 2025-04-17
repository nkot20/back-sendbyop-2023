package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.mappers.ReceiverMapper;
import com.sendByOP.expedition.models.dto.ReceiverDto;
import com.sendByOP.expedition.models.entities.Receiver;
import com.sendByOP.expedition.repositories.ReceiverRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ReceiverService {

    private final ReceiverRepository receveurRepository;
    private final ReceiverMapper receiverMapper;

    public ReceiverDto save(Receiver receiver){
        return receiverMapper
                .toDto(receveurRepository.save(receiver));
    }

}
