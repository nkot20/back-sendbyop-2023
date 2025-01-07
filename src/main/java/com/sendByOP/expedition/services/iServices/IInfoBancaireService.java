package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.InfoBancaireDto;

public interface IInfoBancaireService {

    public InfoBancaireDto save(InfoBancaireDto infoBancaire) throws SendByOpException;
    public InfoBancaireDto getInfoBancaire(int idCli) throws SendByOpException;

}
