package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.Visite;
import com.sendByOP.expedition.repositories.VisiteRepository;
import com.sendByOP.expedition.services.iServices.IVisiteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VisiteService implements IVisiteService {

    private final VisiteRepository visiteRepository;

    @Override
    public Visite addVisitor(Visite visite) {
        log.debug("Adding new visitor");
        return visiteRepository.save(visite);
    }

    @Override
    public int getVisitorCount() {
        log.debug("Getting visitor count");
        return visiteRepository.findAll().size();
    }

}
