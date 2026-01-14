package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.mappers.BankInfoMapper;
import com.sendByOP.expedition.models.dto.BankInfoDto;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.entities.BankInfo;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.repositories.BankAccountRepository;
import com.sendByOP.expedition.repositories.UserRepository;
import com.sendByOP.expedition.services.BankInfoValidationService;
import com.sendByOP.expedition.services.iServices.ICustomerService;
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
    private final ICustomerService clientService;
    private final BankInfoMapper bankInfoMapper;
    private final CustomerMapper customerMapper;
    private final BankInfoValidationService validationService;
    private final UserRepository userRepository;

    @Override
    public BankInfoDto save(BankInfoDto bankInfo) throws SendByOpException {
        log.debug("Saving bank account information for client ID: {}", bankInfo.getClientId());

        // Validate bank account information
        CHeckNull.checkString(bankInfo.getIban());
        //CHeckNull.checkString(bankInfo.getBic());

        // Le clientId reçu est en fait l'ID du User, pas du Customer
        // 1. Récupérer le User par son ID
        User user = userRepository.findById(Long.valueOf(bankInfo.getClientId()))
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                    "User not found with ID: " + bankInfo.getClientId()));

        log.debug("User found with email: {}", user.getEmail());

        // 2. Récupérer le Customer correspondant par email
        CustomerDto customerDto = clientService.getCustomerByEmail(user.getEmail());
        if (customerDto == null) {
            throw new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                "Customer not found for user email: " + user.getEmail());
        }

        log.debug("Customer found with ID: {}", customerDto.getId());
        Customer customer = customerMapper.toEntity(customerDto);
        
        // Convert DTO to entity
        BankInfo newBankInfo = bankInfoMapper.toEntity(bankInfo);
        newBankInfo.setCustomer(customer); // Set the complete Customer object
        
        // Validate uniqueness before saving
        validationService.validateUniqueness(newBankInfo);
        
        // Save the entity
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
                                    ErrorInfo.RESOURCE_NOT_FOUND,
                                    "No bank account information found for this client")
                    );
            
            log.debug("Successfully retrieved bank account information for client ID: {}", clientId);
            return bankInfoMapper.toDto(bankInfo);
        } catch (Exception e) {
            log.error("Error retrieving bank account information for client ID: {}", clientId, e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Error retrieving bank account information");
        }
    }

    @Override
    public BankInfoDto getBankAccountInfosByEmail(String email) throws SendByOpException {
        log.debug("Retrieving bank account information for customer email: {}", email);
        
        try {
            // Récupérer le client par email
            CustomerDto customer = clientService.getCustomerByEmail(email);
            
            // Récupérer les informations bancaires du client
            BankInfo bankInfo = bankAccountRepository.findByCustomer(
                    customerMapper.toEntity(customer))
                    .orElseThrow(
                            () -> new SendByOpException(
                                    ErrorInfo.RESOURCE_NOT_FOUND,
                                    "No bank account information found for customer with email: " + email)
                    );
            
            log.debug("Successfully retrieved bank account information for customer email: {}", email);
            return bankInfoMapper.toDto(bankInfo);
        } catch (SendByOpException e) {
            // Re-lancer les exceptions SendByOp
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving bank account information for customer email: {}", email, e);
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR, "Error retrieving bank account information");
        }
    }
}
