package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.model.Refus;
import com.sendByOP.expedition.repositories.RefusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class RefusService {
    @Autowired
    RefusRepository refusRepository;

    public Refus saveRefus(Refus refus){
        return refusRepository.save(refus);
    }
}
