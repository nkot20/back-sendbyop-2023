package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.services.iServices.IUserService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.repositories.UserRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserService implements IUserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public boolean userIsExist(String username){
        return userRepository.existsByEmail(username);
    }

    @Override
    public User saveUser(User user) throws SendByOpException {
        CHeckNull.checkEmail(user.getEmail());
        CHeckNull.checkLibelle(user.getPw());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) throws SendByOpException {
        CHeckNull.checkEmail(user.getEmail());
        CHeckNull.checkLibelle(user.getPw());
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) throws SendByOpException {
        return userRepository.findByEmail(email).orElseThrow(()-> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND));
    }

    @Override
    public void deleteuser(User user){
        userRepository.delete(user);
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

}
