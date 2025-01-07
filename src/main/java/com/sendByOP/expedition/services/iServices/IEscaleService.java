package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.EscaleDto;
import com.sendByOP.expedition.models.entities.Vol;

import java.util.List;
import java.util.Optional;

public interface IEscaleService {
    EscaleDto addEscale(EscaleDto escaleDTO);
    void deleteEscale(Integer id);
    List<EscaleDto> findByIdvol(Optional<Vol> vol);
}
