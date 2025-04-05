package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.OperationType;
import com.sendByOP.expedition.repositories.OperationTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeOPerationService {

    private final OperationTypeRepository operationRepository;

    public OperationType findTypeById(int id){
        return operationRepository.findById(id).get();
    }

}
