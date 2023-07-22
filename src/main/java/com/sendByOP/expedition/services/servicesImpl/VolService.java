package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.model.Client;
import com.sendByOP.expedition.model.Vol;
import com.sendByOP.expedition.repositories.VolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VolService {
    @Autowired
    VolRepository volRepositorie;

    public Optional<Vol> getVolById(int id){
        return volRepositorie.findById(id);
    }

    public Iterable<Vol> getAllVol(){
        return volRepositorie.findAllByOrderByDatedepartDesc();
    }

    // 1 pour valider et 2 pour rejetter
    public Iterable<Vol> getAllVolValid(int i){
        return volRepositorie.findByEtatvalidation(i);
    }

    public Vol saveVol(Vol vol){
        return volRepositorie.save(vol);
    }

    public void deleteVol(Vol vol){
        volRepositorie.delete(vol);
    }

    public Vol updateVol(Vol vol){
        return volRepositorie.save(vol);
    }

    public List<Vol> getByIdClient(Client idClient){ return volRepositorie.findByIdclientOrderByDatepublicationDesc(idClient); }

    public int nbVolClient(Client idClient){

        List<Vol> vols = volRepositorie.findByIdclientOrderByDatepublicationDesc(idClient);


        vols.removeIf(vol -> vol.getEtatvalidation() != 1);

        return vols.size();
    }

    public Vol getVolByIdVol(int id){
        return volRepositorie.findByIdvol(id);
    }
}
