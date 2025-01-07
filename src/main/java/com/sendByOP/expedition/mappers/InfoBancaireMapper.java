package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.InfoBancaireDto;
import com.sendByOP.expedition.models.entities.InfoBancaire;
import org.mapstruct.*;

/**
 * Mapper for InfoBancaire and InfoBancaireDTO.
 */

public interface InfoBancaireMapper {

    InfoBancaireDto toDTO(InfoBancaire infoBancaire);


    InfoBancaire toEntity(InfoBancaireDto infoBancaireDto);
}
