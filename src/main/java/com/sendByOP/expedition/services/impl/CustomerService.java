package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.repositories.CustomerRepository;
import com.sendByOP.expedition.services.FileStorageService;
import com.sendByOP.expedition.services.iServices.ICustomerService;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerService implements ICustomerService {

    private final CustomerRepository clientRepository;
    private final CustomerMapper customerMapper;
    private final FileStorageService fileStorageService;

    @Override
    public List<CustomerDto> getListClient() {
        return clientRepository.findAll()
                .stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDto getClientById(int id) {
        Customer client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return customerMapper.toDto(client);
    }

    @Override
    public CustomerDto saveClientForUpdatePiD(
            CustomerDto clientDto, MultipartFile imageProfil, MultipartFile imageCni) throws SendByOpException {
        Customer client = customerMapper.toEntity(clientDto);
        CHeckNull.checkEmail(client.getEmail());
        CHeckNull.checkPhoneNumber(client.getPhoneNumber());
        CHeckNull.checkIntitule(client.getPhoneNumber());
        client.setRegistrationStatus(0);
        client.setProfilePicture(transformImage(imageProfil));
        client.setIdentityDocument(transformImage(imageCni));
        Customer savedClient = clientRepository.save(client);
        return customerMapper.toDto(savedClient);
    }

    @Override
    public CustomerDto saveClient(CustomerDto clientDto) throws SendByOpException {
        Customer client = customerMapper.toEntity(clientDto);
        CHeckNull.checkEmail(client.getEmail());
        CHeckNull.checkPhoneNumber(client.getPhoneNumber());
        CHeckNull.checkIntitule(client.getPhoneNumber());
        client.setRegistrationStatus(0);
        client.setEmailVerified(0);
        client.setPhoneVerified(0);
        client.setId(null);
        Customer savedClient = clientRepository.save(client);
        return customerMapper.toDto(savedClient);
    }

    @Override
    @CacheEvict(value = {"customers:email"}, key = "#clientDto.email")
    public CustomerDto updateClient(CustomerDto clientDto) throws SendByOpException {
        log.debug("Updating customer {} and invalidating cache", clientDto.getId());
        Customer existingClient = clientRepository.findById(clientDto.getId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        customerMapper.copy(clientDto, existingClient);
        CHeckNull.checkEmail(existingClient.getEmail());
        CHeckNull.checkPhoneNumber(existingClient.getPhoneNumber());
        CHeckNull.checkIntitule(existingClient.getPhoneNumber());
        Customer updatedClient = clientRepository.save(existingClient);
        return customerMapper.toDto(updatedClient);
    }

    @Override
    public void deleteClient(CustomerDto clientDto) {
        Customer client = customerMapper.toEntity(clientDto);
        clientRepository.delete(client);
    }

    @Override
    public boolean customerIsExist(String email) {
        return clientRepository.existsByEmail(email);
    }

    private String transformImage(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.contains("..")) {
            System.out.println("not a valid file");
        }
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Cacheable(value = "customers:email", key = "#email")
    public CustomerDto getCustomerByEmail(String email) throws SendByOpException {
        log.debug("Fetching customer {} from database (cache miss)", email);
        Customer client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
        return customerMapper.toDto(client);
    }

    @Override
    public CustomerDto getCustomerByEmailRemoveAnyDetails(String email) throws SendByOpException {
        Customer client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
        return customerMapper.toDto(client);
    }


    @Override
    public CustomerDto getCustomerById(int id) throws SendByOpException {
        Customer client = clientRepository.findById(id)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
        return customerMapper.toDto(client);
    }

    @Override
    public List<CustomerDto> getAllRegister() {
        return clientRepository.findAll().stream().map(customerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDto findByNumber(String tel) throws SendByOpException {
        Customer client = clientRepository.findByPhoneNumber(tel)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
        return customerMapper.toDto(client);
    }

    @Override
    public String uploadProfilePicture(Integer customerId, MultipartFile file) throws SendByOpException {
        log.info("Uploading profile picture for customer: {}", customerId);
        
        // Verify customer exists
        Customer customer = clientRepository.findById(customerId)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND));
        
        // Delete old profile picture if exists
        if (customer.getProfilePicture() != null && !customer.getProfilePicture().trim().isEmpty()) {
            fileStorageService.deleteProfilePicture(customer.getProfilePicture());
        }
        
        // Store new profile picture
        String filename = fileStorageService.storeProfilePicture(file, customerId);
        
        // Update customer record
        customer.setProfilePicture(filename);
        clientRepository.save(customer);
        
        log.info("Profile picture uploaded successfully for customer: {}, filename: {}", customerId, filename);
        return filename;
    }
}