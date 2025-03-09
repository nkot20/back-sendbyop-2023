package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Country;
import com.sendByOP.expedition.repositories.PaysRepository;
import com.sendByOP.expedition.services.iServices.IPaysService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaysService implements IPaysService {
    private final PaysRepository paysRepository;

    public Country saveCountry(Country pays){
        return paysRepository.save(pays);
    }

    public List<Country> getCountry(){
        return paysRepository.findAll();
    }

}
