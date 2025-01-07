package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.EscaleMapper;
import com.sendByOP.expedition.models.dto.EscaleDto;
import com.sendByOP.expedition.models.entities.Escale;
import com.sendByOP.expedition.models.entities.Vol;
import com.sendByOP.expedition.repositories.EscaleRepository;
import com.sendByOP.expedition.services.iServices.IEscaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EscaleService implements IEscaleService {

    @Autowired
    public EscaleRepository escaleRepository;

    @Autowired
    private EscaleMapper escaleMapper;

    @Override
    public EscaleDto addEscale(EscaleDto escaleDTO) {
        Escale escale = escaleMapper.toEntity(escaleDTO);
        Escale savedEscale = escaleRepository.save(escale);
        return escaleMapper.toDto(savedEscale);
    }

    @Override
    public void deleteEscale(Integer id) {
        Optional<Escale> escale = escaleRepository.findById(id);
        escale.ifPresent(escaleRepository::delete);
    }

    @Override
    public List<EscaleDto> findByIdvol(Optional<Vol> vol) {
        List<Escale> escales = escaleRepository.findByIdvol(vol);
        return escales.stream()
                .map(escaleMapper::toDto)
                .collect(Collectors.toList());
    }

}
