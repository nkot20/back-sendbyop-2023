package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BankInfoDto;

public interface IInfoBancaireService {

    public BankInfoDto save(BankInfoDto infoBancaire) throws SendByOpException;
    public BankInfoDto getInfoBancaire(int idCli) throws SendByOpException;

}
