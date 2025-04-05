package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.AirportMapper;
import com.sendByOP.expedition.mappers.StopoverMapper;
import com.sendByOP.expedition.mappers.FlightMapper;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.repositories.FlightRepository;
import com.sendByOP.expedition.services.iServices.IVolService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VolService implements IVolService {
    private final FlightRepository volRepository;
    private final FlightMapper volMapper;
    private final AirportMapper aeroportMapper;
    private final StopoverMapper escaleMapper;
    private final StopoverService escaleService;
    private final AirportService aeroportService;

    @Override
    public FlightDto getVolById(int id){
        Flight vol = volRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return volMapper.toDto(vol);
    }

    @Override
    public List<FlightDto> getAllVol(){
        List<Flight> vols = volRepository.findAllByOrderByDatedepartDesc();
        return volMapper.toDtoList(vols);
    }

    @Override
    // 1 pour valider et 2 pour rejetter
    public List<FlightDto> getAllVolValid(int i){
        return volMapper.toDtoList(volRepository.findByEtatvalidation(i));
    }

    @Override
    public FlightDto saveVol(FlightDto vol){
        return volMapper.toDto(volRepository.save(volMapper.toEntity(vol)));
    }

    @Override
    public FlightDto saveVolWithEscales(VolEscaleDto volEscaleDTO) throws SendByOpException {
        // Convert VolEscaleDTO to Vol entity and save it
        Flight volEntity = volMapper.toEntity(volEscaleDTO.getVol());
        volEntity.setIdaeroDepart(aeroportService.getAirport(volEscaleDTO.getVol().getIdaeroDepart().getIdaero()));
        volEntity.setIdAeroArrive(aeroportService.getAirport(volEscaleDTO.getVol().getIdAeroArrive().getIdaero()));

        FlightDto savedVolDTO = saveVol(volMapper.toDto(volEntity));

        // Convert the saved Vol entity to VolDTO

        // If there are escales, process each one
        if (volEscaleDTO.getEscales() != null && !volEscaleDTO.getEscales().isEmpty()) {
            volEscaleDTO.getEscales().forEach(escaleDTO -> {
                escaleDTO.setIdvol(savedVolDTO);
                escaleService.addStopover(escaleDTO); // Save the escale entity
            });
        }

        return savedVolDTO;
    }


    @Override
    public void deleteVol(int id){
        volRepository.deleteById(id);
    }

    @Override
    public FlightDto updateVol(FlightDto vol){
        return volMapper.toDto(volRepository.save(volMapper.toEntity(vol)));
    }

    @Override
    public List<FlightDto> getByIdClient(CustomerDto idClient){
        return volMapper.toDtoList(volRepository
                .findByIdclientOrderByDatepublicationDesc(idClient.getIdp()));
    }

    @Override
    public int nbVolClient(CustomerDto idClient){

        List<Flight> vols = volRepository.findByIdclientOrderByDatepublicationDesc(idClient.getIdp());


        vols.removeIf(vol -> vol.getEtatvalidation() != 1);

        return vols.size();
    }

    @Override
    public FlightDto getVolByIdVol(int id){
        return volMapper.toDto(volRepository.findByIdvol(id).orElseThrow(EntityNotFoundException::new));
    }
}
