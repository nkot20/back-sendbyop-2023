package com.sendByOP.expedition.services.iServices;


import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;

import java.util.List;

public interface IVolService {
    FlightDto getVolById(int id);

    Iterable<FlightDto> getAllVol();

    Iterable<FlightDto> getAllVolValid(int i);

    FlightDto saveVol(FlightDto vol);

    FlightDto saveVolWithEscales(VolEscaleDto volEscaleDTO) throws SendByOpException;

    void deleteVol(int id);

    FlightDto updateVol(FlightDto vol);

    List<FlightDto> getByIdClient(CustomerDto idClient);

    int nbVolClient(CustomerDto idClient);

    FlightDto getVolByIdVol(int id);
}
