package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.model.Escale;
import com.sendByOP.expedition.model.Vol;
import com.sendByOP.expedition.repositories.EscaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EscaleService {

    @Autowired
    public EscaleRepository escaleRepositorie;

    public Escale addEscale(Escale escale){
        return escaleRepositorie.save(escale);
    }

    public void deleteEscale(Escale escale){
        escaleRepositorie.delete(escale);
    }

    public List<Escale> findByIdvol(Optional<Vol> vol){
        return escaleRepositorie.findByIdvol(vol);
    }

}
