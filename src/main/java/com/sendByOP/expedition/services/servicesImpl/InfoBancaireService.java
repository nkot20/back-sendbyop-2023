package com.sendByOP.expedition.services.servicesImpl;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.InfoBancaireMapper;
import com.sendByOP.expedition.models.dto.InfoBancaireDto;
import com.sendByOP.expedition.models.entities.InfoBancaire;
import com.sendByOP.expedition.repositories.InfoBancaireRepository;
import com.sendByOP.expedition.services.iServices.IClientServivce;
import com.sendByOP.expedition.services.iServices.IInfoBancaireService;
import com.sendByOP.expedition.utils.CHeckNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InfoBancaireService implements IInfoBancaireService {

    private final InfoBancaireRepository infoBancaireRepository;

    private final IClientServivce iClientServivce;

    private final InfoBancaireMapper infoBancaireMapper;

    @Override
    public InfoBancaireDto save(InfoBancaireDto infoBancaire) throws SendByOpException {
        CHeckNull.checkString(infoBancaire.getIban());
        CHeckNull.checkString(infoBancaire.getBic());
        InfoBancaire newBankInfos = infoBancaireMapper.toEntity(infoBancaire);
        return infoBancaireMapper.toDTO(
                infoBancaireRepository.save(newBankInfos));
    }

    @Override
    public InfoBancaireDto getInfoBancaire(int idCli) throws SendByOpException {

        InfoBancaire infoBancaire = infoBancaireRepository.findInfoBancaireByIdclient(
                iClientServivce.getClientById(idCli))
                .orElseThrow(
                        ()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND, "Aucune information bancaire pour ce client")
                );
        System.out.println(infoBancaire);
        return infoBancaireMapper.toDTO(infoBancaire);
    }
}
