package com.sendByOP.expedition.mappers.mapperImplement;

import com.sendByOP.expedition.mappers.EscaleMapper;
import com.sendByOP.expedition.models.dto.AeroPortDto;
import com.sendByOP.expedition.models.dto.EscaleDto;
import com.sendByOP.expedition.models.dto.VolDto;
import com.sendByOP.expedition.models.entities.Aeroport;
import com.sendByOP.expedition.models.entities.Escale;
import com.sendByOP.expedition.models.entities.Vol;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class EscaleMapperManual implements EscaleMapper {

    public Escale toEntity(EscaleDto escaleDTO) {
        if (escaleDTO == null) {
            return null;
        }

        Escale escale = new Escale();
        escale.setIdEscale(escaleDTO.getIdEscale());
        escale.setDatees(escaleDTO.getDatees());
        escale.setHeure(escaleDTO.getHeure());

        // Mapping manuel des objets imbriqués (Aeroport et Vol)
        if (escaleDTO.getIdaero() != null) {
            Aeroport aeroport = new Aeroport();
            aeroport.setIdaero(escaleDTO.getIdaero().getIdaero());
            escale.setIdaero(aeroport);
        }

        if (escaleDTO.getIdvol() != null) {
            Vol vol = new Vol();
            vol.setIdvol(escaleDTO.getIdvol().getIdvol());
            escale.setIdvol(vol);
        }

        return escale;
    }

    public EscaleDto toDto(Escale escale) {
        if (escale == null) {
            return null;
        }

        EscaleDto escaleDTO = new EscaleDto();
        escaleDTO.setIdEscale(escale.getIdEscale());
        escaleDTO.setDatees(escale.getDatees());
        escaleDTO.setHeure(escale.getHeure());

        // Mapping manuel des objets imbriqués (AeroportDTO et VolDTO)
        if (escale.getIdaero() != null) {
            AeroPortDto aeroportDTO = new AeroPortDto();
            aeroportDTO.setIdaero(escale.getIdaero().getIdaero());
            escaleDTO.setIdaero(aeroportDTO);
        }

        if (escale.getIdvol() != null) {
            VolDto volDTO = new VolDto();
            volDTO.setIdvol(escale.getIdvol().getIdvol());
            escaleDTO.setIdvol(volDTO);
        }

        return escaleDTO;
    }

    public void copy(EscaleDto escaleDTO, Escale escale) {
        if (escaleDTO == null || escale == null) {
            return;
        }

        escale.setIdEscale(escaleDTO.getIdEscale());
        escale.setDatees(escaleDTO.getDatees());
        escale.setHeure(escaleDTO.getHeure());

        // Mapping manuel des objets imbriqués (Aeroport et Vol)
        if (escaleDTO.getIdaero() != null) {
            if (escale.getIdaero() == null) {
                escale.setIdaero(new Aeroport());
            }
            escale.getIdaero().setIdaero(escaleDTO.getIdaero().getIdaero());
        }

        if (escaleDTO.getIdvol() != null) {
            if (escale.getIdvol() == null) {
                escale.setIdvol(new Vol());
            }
            escale.getIdvol().setIdvol(escaleDTO.getIdvol().getIdvol());
        }
    }
}
