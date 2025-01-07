package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.services.iServices.IClientServivce;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.repositories.ClientReopository;
import com.sendByOP.expedition.utils.CHeckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class Clientservice implements IClientServivce {
    @Autowired
    ClientReopository clientReopository;

    @Autowired
    SendMailService sendMailService;

    @Override
    public List<Client> getListClient() {
        return clientReopository.findAll();
    }

    @Override
    public Client getClientById(int id){
        return clientReopository.getById(id);
    }

    @Override
    public Client saveClientForUpdatePiD(Client client, MultipartFile imageProfil, MultipartFile imageCni) throws SendByOpException {
        CHeckNull.checkEmail(client.getEmail());
        CHeckNull.checkNumero(client.getNumcni());
        CHeckNull.checkIntitule(client.getTel());
        client.setEtatInscription(0);
        client.setPhotoProfil(transfomrImage(imageProfil));
        client.setPieceid(transfomrImage(imageCni));
        return clientReopository.save(client);
    }

    @Override
    public Client saveClient(Client client) throws SendByOpException {
        CHeckNull.checkEmail(client.getEmail());
        CHeckNull.checkNumero(client.getNumcni());
        CHeckNull.checkIntitule(client.getTel());
        client.setEtatInscription(0);
        client.setValidEmail(0);
        client.setValidNumber(0);
        client.setIdp(null);
        return clientReopository.save(client);
    }

    @Override
    public Client updateClient(Client client) throws SendByOpException {
        CHeckNull.checkEmail(client.getEmail());
        CHeckNull.checkNumero(client.getNumcni());
        CHeckNull.checkIntitule(client.getTel());
        return clientReopository.save(client);
    }

    @Override
    public void deleteClient(Client client){
        clientReopository.delete(client);
    }

    @Override
    public boolean customerIsExist(String email){
        return clientReopository.existsByEmail(email);
    }

    //Transformer l'image en chaine
    private String transfomrImage(MultipartFile file){
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(fileName.contains(".."))
        {
            System.out.println("not a a valid file");
        }
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Client getCustomerByEmail(String email) throws SendByOpException {
        return clientReopository.findByEmail(email).orElseThrow(()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }

    @Override
    public Client getCustomerByEmailRemoveAnyDetails(String email) throws SendByOpException {
        Client client = clientReopository.findByEmail(email).orElseThrow(()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
        client.setPw(null);
        client.setDatenais(null);
        client.setIban(null);
        client.setNumcni(0);
        return client;
    }

    @Override
    public Client getCustomerById(int id) throws SendByOpException {
        Client client = clientReopository.findById(id).orElseThrow(()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
        client.setPw(null);
        client.setDatenais(null);
        client.setIban(null);
        client.setNumcni(0);
        return client;
    }

    @Override
    public List<Client> getAllRegister(){
        return clientReopository.findAll();
    }

    @Override
    public Client findByNumber(String tel) throws SendByOpException {
       return this.clientReopository.findByTel(tel).orElseThrow(()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }
}
