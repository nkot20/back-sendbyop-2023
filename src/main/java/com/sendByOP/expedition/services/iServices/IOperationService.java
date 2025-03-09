package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.OperationDto;
import com.sendByOP.expedition.models.dto.BookingDto;

import java.util.List;

public interface IOperationService {
    public OperationDto saveOperation(OperationDto operation, int typeId);
    public OperationDto searchOperation(int id);
    public void deleteOperation(OperationDto operation);
    public void deleteOperation(int operationId);
    public List<OperationDto> findOperationByType(OperationDto operation);
    public BookingDto enregistrerDepotParExpediteur (int id) throws Exception;
    public BookingDto saveDepotParClient(int id) throws Exception;

}
