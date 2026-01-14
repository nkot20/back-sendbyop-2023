package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CustomerDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICustomerService {

    public List<CustomerDto> getListClient();
    public CustomerDto getClientById(int id);
    public CustomerDto saveClientForUpdatePiD(CustomerDto client, MultipartFile imageProfil, MultipartFile imageCni) throws SendByOpException;
    public CustomerDto saveClient(CustomerDto client) throws SendByOpException;
    public CustomerDto updateClient(CustomerDto client) throws SendByOpException;
    public void deleteClient(CustomerDto client);
    public boolean customerIsExist(String email);
    public CustomerDto getCustomerByEmail(String email) throws SendByOpException;
    public CustomerDto getCustomerByEmailRemoveAnyDetails(String email) throws SendByOpException;
    public CustomerDto getCustomerById(int id) throws SendByOpException;
    public List<CustomerDto> getAllRegister();
    public CustomerDto findByNumber(String tel) throws SendByOpException;
    public String uploadProfilePicture(Integer customerId, MultipartFile file) throws SendByOpException;


}
