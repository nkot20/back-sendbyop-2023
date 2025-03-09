package com.sendByOP.expedition.services.servicesImpl;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.mappers.BankInfoMapper;
import com.sendByOP.expedition.models.dto.BankInfoDto;
import com.sendByOP.expedition.models.entities.BankInfo;
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
    private final BankInfoMapper infoBancaireMapper;
    private final CustomerMapper customerMapper;

    @Override
    public BankInfoDto save(BankInfoDto infoBancaire) throws SendByOpException {
        CHeckNull.checkString(infoBancaire.getIban());
        CHeckNull.checkString(infoBancaire.getBic());
        BankInfo newBankInfos = infoBancaireMapper.toEntity(infoBancaire);
        return infoBancaireMapper.toDTO(
                infoBancaireRepository.save(newBankInfos));
    }

    @Override
    public BankInfoDto getInfoBancaire(int idCli) throws SendByOpException {

        BankInfo infoBancaire = infoBancaireRepository.findInfoBancaireByIdclient(
                customerMapper.toEntity(iClientServivce.getClientById(idCli)))
                .orElseThrow(
                        ()-> new SendByOpException(
                                ErrorInfo.RESSOURCE_NOT_FOUND,
                                "Aucune information bancaire pour ce client")
                );
        System.out.println(infoBancaire);
        return infoBancaireMapper.toDTO(infoBancaire);
    }
}
