package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.ReceiverMapper;
import com.sendByOP.expedition.models.dto.ReceiverDto;
import com.sendByOP.expedition.models.entities.Receiver;
import com.sendByOP.expedition.repositories.ReceveurRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ReceveurService {

    private final ReceveurRepository receveurRepository;
    private final ReceiverMapper receiverMapper;

    public ReceiverDto save(Receiver receveur){
        return receiverMapper.toDto(receveurRepository.save(receveur));
    }

}
