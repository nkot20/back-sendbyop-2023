package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BankInfoDto;

public interface IBankAccountService {

    public BankInfoDto save(BankInfoDto infoBancaire) throws SendByOpException;
    public BankInfoDto getBankAccountInfos(int idCli) throws SendByOpException;

}
