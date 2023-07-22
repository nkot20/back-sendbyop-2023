package com.sendByOP.expedition.services.servicesImpl;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.InfoBancaire;
import com.sendByOP.expedition.repositories.InfoBancaireRepository;
import com.sendByOP.expedition.services.IServices.IClientServivce;
import com.sendByOP.expedition.services.IServices.IInfoBancaireService;
import com.sendByOP.expedition.utils.CHeckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InfoBancaireService implements IInfoBancaireService {

    @Autowired
    InfoBancaireRepository infoBancaireRepository;
    @Autowired
    IClientServivce iClientServivce;

    @Override
    public InfoBancaire save(InfoBancaire infoBancaire) throws SendByOpException {
        CHeckNull.checkString(infoBancaire.getIban());
        CHeckNull.checkString(infoBancaire.getBic());
        return infoBancaireRepository.save(infoBancaire);
    }

    @Override
    public InfoBancaire getInfoBancaire(int idCli) throws SendByOpException {

        return infoBancaireRepository.findInfoBancaireByIdclient(iClientServivce.getClientById(idCli)).orElseThrow(()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }
}
