package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Typeoperation;
import com.sendByOP.expedition.repositories.TypeOperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class TypeOPerationService {

    @Autowired
    TypeOperationRepository operationRepository;

    public Typeoperation findTypeById(int id){
        return operationRepository.findByIdtypeoperation(id).get();
    }

}
