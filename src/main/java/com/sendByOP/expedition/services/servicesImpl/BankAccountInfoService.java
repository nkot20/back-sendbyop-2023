package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.mappers.BankInfoMapper;
import com.sendByOP.expedition.models.dto.BankInfoDto;
import com.sendByOP.expedition.models.entities.BankInfo;
import com.sendByOP.expedition.repositories.BankAccountRepository;
import com.sendByOP.expedition.services.iServices.IClientServivce;
import com.sendByOP.expedition.services.iServices.IBankAccountService;
import com.sendByOP.expedition.utils.CHeckNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountInfoService implements IBankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final IClientServivce clientService;
    private final BankInfoMapper bankInfoMapper;
    private final CustomerMapper customerMapper;

    @Override
    public BankInfoDto save(BankInfoDto bankInfo) throws SendByOpException {
        log.debug("Saving bank account information");
        
        // Validate bank account information
        CHeckNull.checkString(bankInfo.getIban());
        CHeckNull.checkString(bankInfo.getBic());
        
        // Convert DTO to entity and save
        BankInfo newBankInfo = bankInfoMapper.toEntity(bankInfo);
        BankInfo savedBankInfo = bankAccountRepository.save(newBankInfo);
        
        log.debug("Successfully saved bank account information with ID: {}", savedBankInfo.getId());
        return bankInfoMapper.toDto(savedBankInfo);
    }

    @Override
    public BankInfoDto getBankAccountInfos(int clientId) throws SendByOpException {
        log.debug("Retrieving bank account information for client ID: {}", clientId);
        
        try {
            BankInfo bankInfo = bankAccountRepository.findByCustomer(
                    customerMapper.toEntity(clientService.getClientById(clientId)))
                    .orElseThrow(
                            () -> new SendByOpException(
                                    ErrorInfo.RESSOURCE_NOT_FOUND,
                                    "No bank account information found for this client")
                    );
            
            log.debug("Successfully retrieved bank account information for client ID: {}", clientId);
            return bankInfoMapper.toDto(bankInfo);
        } catch (Exception e) {
            log.error("Error retrieving bank account information for client ID: {}", clientId, e);
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR, "Error retrieving bank account information");
        }
    }
}
