package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Client;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IClientServivce {

    public List<Client> getListClient();
    public Client getClientById(int id);
    public Client saveClientForUpdatePiD(Client client, MultipartFile imageProfil, MultipartFile imageCni) throws SendByOpException;
    public Client saveClient(Client client) throws SendByOpException;
    public Client updateClient(Client client) throws SendByOpException;
    public void deleteClient(Client client);
    public boolean customerIsExist(String email);
    public Client getCustomerByEmail(String email) throws SendByOpException;
    public Client getCustomerByEmailRemoveAnyDetails(String email) throws SendByOpException;
    public Client getCustomerById(int id) throws SendByOpException;
    public List<Client> getAllRegister();
    public Client findByNumber(String tel) throws SendByOpException;

}
