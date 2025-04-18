package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.OperationDto;
import com.sendByOP.expedition.models.dto.BookingDto;

import java.util.List;

public interface IOperationService {
    public OperationDto saveOperation(OperationDto operation, int typeId) throws SendByOpException;
    public OperationDto searchOperation (int id) throws SendByOpException;
    public void deleteOperation(OperationDto operation) throws SendByOpException;
    public void deleteOperation(int operationId) throws SendByOpException;
    public BookingDto registerSenderDeposit(int id) throws Exception;
    public BookingDto registerCustomerDeposit(int id) throws Exception;

}
