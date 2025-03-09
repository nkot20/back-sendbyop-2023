package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.StopoverMapper;
import com.sendByOP.expedition.models.dto.StopoverDto;
import com.sendByOP.expedition.models.entities.Stopover;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.repositories.EscaleRepository;
import com.sendByOP.expedition.services.iServices.IEscaleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EscaleService implements IEscaleService {
    private final EscaleRepository escaleRepository;
    private final StopoverMapper escaleMapper;

    @Override
    public StopoverDto addEscale(StopoverDto escaleDTO) {
        Stopover escale = escaleMapper.toEntity(escaleDTO);
        Stopover savedEscale = escaleRepository.save(escale);
        return escaleMapper.toDto(savedEscale);
    }

    @Override
    public void deleteEscale(Integer id) {
        Optional<Stopover> escale = escaleRepository.findById(id);
        escale.ifPresent(escaleRepository::delete);
    }

    @Override
    public List<StopoverDto> findByIdvol(Optional<Flight> vol) {
        List<Stopover> escales = escaleRepository.findByIdvol(vol);
        return escales.stream()
                .map(escaleMapper::toDto)
                .collect(Collectors.toList());
    }

}
