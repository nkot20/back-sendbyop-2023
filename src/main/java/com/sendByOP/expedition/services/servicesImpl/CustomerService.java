package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.repositories.ClientReopository;
import com.sendByOP.expedition.services.iServices.IClientServivce;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService implements IClientServivce {

    private final ClientReopository clientRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDto> getListClient() {
        return customerMapper.toDtoList(clientRepository.findAll());
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
        CHeckNull.checkNumero(client.getNumcni());
        CHeckNull.checkIntitule(client.getTel());
        client.setEtatInscription(0);
        client.setPhotoProfil(transformImage(imageProfil));
        client.setPieceid(transformImage(imageCni));
        Customer savedClient = clientRepository.save(client);
        return customerMapper.toDto(savedClient);
    }

    @Override
    public CustomerDto saveClient(CustomerDto clientDto) throws SendByOpException {
        Customer client = customerMapper.toEntity(clientDto);
        CHeckNull.checkEmail(client.getEmail());
        CHeckNull.checkNumero(client.getNumcni());
        CHeckNull.checkIntitule(client.getTel());
        client.setEtatInscription(0);
        client.setValidEmail(0);
        client.setValidNumber(0);
        client.setIdp(null);
        Customer savedClient = clientRepository.save(client);
        return customerMapper.toDto(savedClient);
    }

    @Override
    public CustomerDto updateClient(CustomerDto clientDto) throws SendByOpException {
        Customer existingClient = clientRepository.findById(clientDto.getIdp())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        customerMapper.copy(clientDto, existingClient);
        CHeckNull.checkEmail(existingClient.getEmail());
        CHeckNull.checkNumero(existingClient.getNumcni());
        CHeckNull.checkIntitule(existingClient.getTel());
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
    public CustomerDto getCustomerByEmail(String email) throws SendByOpException {
        Customer client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
        return customerMapper.toDto(client);
    }

    @Override
    public CustomerDto getCustomerByEmailRemoveAnyDetails(String email) throws SendByOpException {
        Customer client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
        client.setPw(null);
        client.setDatenais(null);
        client.setIban(null);
        client.setNumcni(0);
        return customerMapper.toDto(client);
    }

    @Override
    public CustomerDto getCustomerById(int id) throws SendByOpException {
        Customer client = clientRepository.findById(id)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
        client.setPw(null);
        client.setDatenais(null);
        client.setIban(null);
        client.setNumcni(0);
        return customerMapper.toDto(client);
    }

    @Override
    public List<CustomerDto> getAllRegister() {
        return customerMapper.toDtoList(clientRepository.findAll());
    }

    @Override
    public CustomerDto findByNumber(String tel) throws SendByOpException {
        Customer client = clientRepository.findByTel(tel)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
        return customerMapper.toDto(client);
    }
}