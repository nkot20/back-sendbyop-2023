package com.sendByOP.expedition.mappers.mapperImplement;

import com.sendByOP.expedition.mappers.InfoBancaireMapper;
import com.sendByOP.expedition.models.dto.InfoBancaireDto;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.InfoBancaire;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class InfoBancaireMapperManual implements InfoBancaireMapper {

    /**
     * Convert InfoBancaire entity to InfoBancaireDTO using Builder.
     *
     * @param infoBancaire the InfoBancaire entity
     * @return the corresponding InfoBancaireDTO
     */
    public InfoBancaireDto toDTO(InfoBancaire infoBancaire) {
        if (infoBancaire == null) {
            return null;
        }

        return InfoBancaireDto.builder()
                .idInfo(infoBancaire.getIdInfo())
                .idClient(infoBancaire.getIdclient() != null ? infoBancaire.getIdclient().getIdp() : null)
                .iban(infoBancaire.getIban())
                .countryName(infoBancaire.getCountryname())
                .bankAccount(infoBancaire.getBankAccount())
                .bankName(infoBancaire.getBankName())
                .bic(infoBancaire.getBic())
                .accountHolder(infoBancaire.getAccountHolder())
                .build();
    }

    /**
     * Convert InfoBancaireDTO to InfoBancaire entity using Builder.
     *
     * @param dto the InfoBancaireDTO
     * @return the corresponding InfoBancaire entity
     */
    public InfoBancaire toEntity(InfoBancaireDto dto) {
        if (dto == null) {
            return null;
        }

        InfoBancaire infoBancaire = new InfoBancaire();
        infoBancaire.setIdInfo(dto.getIdInfo());

        // Map idClient to Client entity if available
        if (dto.getIdClient() != null) {
            Client client = new Client();
            client.setIdp(dto.getIdClient());
            infoBancaire.setIdclient(client);
        }

        infoBancaire.setIban(dto.getIban());
        infoBancaire.setCountryname(dto.getCountryName());
        infoBancaire.setBankAccount(dto.getBankAccount());
        infoBancaire.setBankName(dto.getBankName());
        infoBancaire.setBic(dto.getBic());
        infoBancaire.setAccountHolder(dto.getAccountHolder());

        return infoBancaire;
    }
}
