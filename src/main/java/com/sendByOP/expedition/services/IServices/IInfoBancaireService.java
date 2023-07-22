package com.sendByOP.expedition.services.IServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.InfoBancaire;

public interface IInfoBancaireService {

    public InfoBancaire save(InfoBancaire infoBancaire) throws SendByOpException;
    public InfoBancaire getInfoBancaire(int idCli) throws SendByOpException;

}
