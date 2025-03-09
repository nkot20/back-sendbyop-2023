package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.OperationType;
import com.sendByOP.expedition.repositories.TypeOperationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TypeOPerationService {

    @Autowired
    TypeOperationRepository operationRepository;

    public OperationType findTypeById(int id){
        return operationRepository.findByIdtypeoperation(id).get();
    }

}
