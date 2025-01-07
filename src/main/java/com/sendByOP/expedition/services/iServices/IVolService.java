package com.sendByOP.expedition.services.iServices;


import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.VolDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Vol;

import java.util.List;
import java.util.Optional;

public interface IVolService {
    Optional<Vol> getVolById(int id);

    Iterable<Vol> getAllVol();

    Iterable<Vol> getAllVolValid(int i);

    Vol saveVol(Vol vol);

    VolDto saveVolWithEscales(VolEscaleDto volEscaleDTO) throws SendByOpException;

    void deleteVol(Vol vol);

    Vol updateVol(Vol vol);

    List<Vol> getByIdClient(Client idClient);

    int nbVolClient(Client idClient);

    Vol getVolByIdVol(int id);
}
