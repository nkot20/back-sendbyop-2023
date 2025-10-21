package com.sendByOP.expedition.services.iServices;


import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.PublicFlightDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IVolService {
    FlightDto getVolById(int id);

    List<FlightDto> getAllVol();

    List<FlightDto> getAllVolValid(int i);

    FlightDto saveVol(FlightDto vol);

    FlightDto saveVolWithEscales(VolEscaleDto volEscaleDTO) throws SendByOpException;

    void deleteVol(int id) throws SendByOpException;

    FlightDto updateVol(FlightDto vol) throws SendByOpException;

    List<FlightDto> getByIdClient(CustomerDto idClient) throws SendByOpException;

    int nbVolClient(CustomerDto idClient) throws SendByOpException;

    FlightDto getVolByIdVol(int id);
    
    List<FlightDto> getValidAndActiveFlights();
    
    List<PublicFlightDto> getPublicValidAndActiveFlights();
    
    Page<PublicFlightDto> getPublicValidAndActiveFlights(Pageable pageable);
    
    PublicFlightDto getPublicFlightById(int id);
}
