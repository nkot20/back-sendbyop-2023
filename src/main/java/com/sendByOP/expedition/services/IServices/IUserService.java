package com.sendByOP.expedition.services.IServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.model.User;

import java.util.List;

public interface IUserService {
    public boolean userIsExist(String username);
    public User saveUser(User user) throws SendByOpException;
    public User updateUser(User user) throws SendByOpException;
    public User findByEmail(String email) throws SendByOpException;
    public void deleteuser(User user);
    public List<User> getAllUser();
}
