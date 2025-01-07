package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.EscaleDto;
import com.sendByOP.expedition.models.entities.Escale;
import org.mapstruct.*;

 // Utilisation des autres mappers
public interface EscaleMapper {


    Escale toEntity(EscaleDto escaleDTO);

    EscaleDto toDto(Escale escale);

    void copy(EscaleDto escaleDTO, @MappingTarget Escale escale);
}
