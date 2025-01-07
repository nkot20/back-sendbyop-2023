package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.AeroportMapper;
import com.sendByOP.expedition.mappers.EscaleMapper;
import com.sendByOP.expedition.mappers.VolMapper;
import com.sendByOP.expedition.models.dto.VolDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Vol;
import com.sendByOP.expedition.repositories.VolRepository;
import com.sendByOP.expedition.services.iServices.IVolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VolService implements IVolService {
    @Autowired
    VolRepository volRepository;
    @Autowired
    VolMapper volMapper;
    @Autowired
    AeroportMapper aeroportMapper;

    @Autowired
    EscaleMapper escaleMapper;

    @Autowired
    EscaleService escaleService;

    @Autowired
    AeroportService aeroportService;

    @Override
    public Optional<Vol> getVolById(int id){
        return volRepository.findById(id);
    }

    @Override
    public Iterable<Vol> getAllVol(){
        return volRepository.findAllByOrderByDatedepartDesc();
    }

    @Override
    // 1 pour valider et 2 pour rejetter
    public Iterable<Vol> getAllVolValid(int i){
        return volRepository.findByEtatvalidation(i);
    }

    @Override
    public Vol saveVol(Vol vol){ return volRepository.save(vol); }

    @Override
    public VolDto saveVolWithEscales(VolEscaleDto volEscaleDTO) throws SendByOpException {
        // Convert VolEscaleDTO to Vol entity and save it
        Vol volEntity = volMapper.toEntity(volEscaleDTO.getVol());
        volEntity.setIdaeroDepart(aeroportService.getAirport(volEscaleDTO.getVol().getIdaeroDepart().getIdaero()));
        volEntity.setIdAeroArrive(aeroportService.getAirport(volEscaleDTO.getVol().getIdAeroArrive().getIdaero()));
        Vol savedVolEntity = saveVol(volEntity);

        // Convert the saved Vol entity to VolDTO
        VolDto savedVolDTO = volMapper.toDto(savedVolEntity);

        // If there are escales, process each one
        if (volEscaleDTO.getEscales() != null && !volEscaleDTO.getEscales().isEmpty()) {
            volEscaleDTO.getEscales().forEach(escaleDTO -> {
                escaleDTO.setIdvol(savedVolDTO);
                escaleService.addEscale(escaleDTO); // Save the escale entity
            });
        }

        return savedVolDTO;
    }


    @Override
    public void deleteVol(Vol vol){
        volRepository.delete(vol);
    }

    @Override
    public Vol updateVol(Vol vol){
        return volRepository.save(vol);
    }

    @Override
    public List<Vol> getByIdClient(Client idClient){ return volRepository.findByIdclientOrderByDatepublicationDesc(idClient); }

    @Override
    public int nbVolClient(Client idClient){

        List<Vol> vols = volRepository.findByIdclientOrderByDatepublicationDesc(idClient);


        vols.removeIf(vol -> vol.getEtatvalidation() != 1);

        return vols.size();
    }

    @Override
    public Vol getVolByIdVol(int id){
        return volRepository.findByIdvol(id);
    }
}
