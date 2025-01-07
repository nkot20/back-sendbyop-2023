package com.sendByOP.expedition.mappers.mapperImplement;

import com.sendByOP.expedition.mappers.VolMapper;
import com.sendByOP.expedition.models.dto.AeroPortDto;
import com.sendByOP.expedition.models.dto.ClientDto;
import com.sendByOP.expedition.models.dto.VolDto;
import com.sendByOP.expedition.models.entities.Aeroport;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Vol;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class VolMapperManual implements VolMapper {

    @Override
    public Vol toEntity(VolDto volDTO) {
        if (volDTO == null) {
            return null;
        }

        Vol vol = new Vol();
        vol.setIdvol(volDTO.getIdvol());
        vol.setDatearrive(volDTO.getDatearrive());
        vol.setDatedepart(volDTO.getDatedepart());
        vol.setHeurearriv(volDTO.getHeurearriv());
        vol.setHeuredepart(volDTO.getHeuredepart());
        vol.setMontantkilo(volDTO.getMontantkilo());
        vol.setNbkilo(volDTO.getNbkilo());
        vol.setEtatvalidation(volDTO.getEtatvalidation());
        vol.setPreference(volDTO.getPreference());
        vol.setDatepublication(volDTO.getDatepublication());
        vol.setImage(volDTO.getImage());
        vol.setLieuDepot(volDTO.getLieuDepot());
        vol.setLieuReception(volDTO.getLieuReception());
        vol.setAnnuler(volDTO.getAnnuler());

        // Mapping manuel pour les objets imbriqués Aeroport et Client
        if (volDTO.getIdaeroDepart() != null) {
            Aeroport aeroportDepart = new Aeroport();
            aeroportDepart.setIdaero(volDTO.getIdaeroDepart().getIdaero());
            vol.setIdaeroDepart(aeroportDepart);
        }

        if (volDTO.getIdAeroArrive() != null) {
            Aeroport aeroportArrive = new Aeroport();
            aeroportArrive.setIdaero(volDTO.getIdAeroArrive().getIdaero());
            vol.setIdAeroArrive(aeroportArrive);
        }

        if (volDTO.getIdclient() != null) {
            Client client = new Client();
            client.setIdp(volDTO.getIdclient().getIdp());
            vol.setIdclient(client);
        }

        return vol;
    }

    @Override
    public VolDto toDto(Vol vol) {
        if (vol == null) {
            return null;
        }

        VolDto volDTO = new VolDto();
        volDTO.setIdvol(vol.getIdvol());
        volDTO.setDatearrive(vol.getDatearrive());
        volDTO.setDatedepart(vol.getDatedepart());
        volDTO.setHeurearriv(vol.getHeurearriv());
        volDTO.setHeuredepart(vol.getHeuredepart());
        volDTO.setMontantkilo(vol.getMontantkilo());
        volDTO.setNbkilo(vol.getNbkilo());
        volDTO.setEtatvalidation(vol.getEtatvalidation());
        volDTO.setPreference(vol.getPreference());
        volDTO.setDatepublication(vol.getDatepublication());
        volDTO.setImage(vol.getImage());
        volDTO.setLieuDepot(vol.getLieuDepot());
        volDTO.setLieuReception(vol.getLieuReception());
        volDTO.setAnnuler(vol.getAnnuler());

        // Mapping manuel pour les objets imbriqués AeroPortDTO et ClientDTO
        if (vol.getIdaeroDepart() != null) {
            AeroPortDto aeroportDepartDTO = new AeroPortDto();
            aeroportDepartDTO.setIdaero(vol.getIdaeroDepart().getIdaero());
            volDTO.setIdaeroDepart(aeroportDepartDTO);
        }

        if (vol.getIdAeroArrive() != null) {
            AeroPortDto aeroportArriveDTO = new AeroPortDto();
            aeroportArriveDTO.setIdaero(vol.getIdAeroArrive().getIdaero());
            volDTO.setIdAeroArrive(aeroportArriveDTO);
        }

        if (vol.getIdclient() != null) {
            ClientDto clientDTO = new ClientDto();
            clientDTO.setIdp(vol.getIdclient().getIdp());
            volDTO.setIdclient(clientDTO);
        }

        return volDTO;
    }

    @Override
    public void copy(VolDto volDTO, Vol vol) {
        if (volDTO == null || vol == null) {
            return;
        }

        vol.setIdvol(volDTO.getIdvol());
        vol.setDatearrive(volDTO.getDatearrive());
        vol.setDatedepart(volDTO.getDatedepart());
        vol.setHeurearriv(volDTO.getHeurearriv());
        vol.setHeuredepart(volDTO.getHeuredepart());
        vol.setMontantkilo(volDTO.getMontantkilo());
        vol.setNbkilo(volDTO.getNbkilo());
        vol.setEtatvalidation(volDTO.getEtatvalidation());
        vol.setPreference(volDTO.getPreference());
        vol.setDatepublication(volDTO.getDatepublication());
        vol.setImage(volDTO.getImage());
        vol.setLieuDepot(volDTO.getLieuDepot());
        vol.setLieuReception(volDTO.getLieuReception());
        vol.setAnnuler(volDTO.getAnnuler());

        // Copie des objets imbriqués
        if (volDTO.getIdaeroDepart() != null) {
            if (vol.getIdaeroDepart() == null) {
                vol.setIdaeroDepart(new Aeroport());
            }
            vol.getIdaeroDepart().setIdaero(volDTO.getIdaeroDepart().getIdaero());
        }

        if (volDTO.getIdAeroArrive() != null) {
            if (vol.getIdAeroArrive() == null) {
                vol.setIdAeroArrive(new Aeroport());
            }
            vol.getIdAeroArrive().setIdaero(volDTO.getIdAeroArrive().getIdaero());
        }

        if (volDTO.getIdclient() != null) {
            if (vol.getIdclient() == null) {
                vol.setIdclient(new Client());
            }
            vol.getIdclient().setIdp(volDTO.getIdclient().getIdp());
        }
    }
}
