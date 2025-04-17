package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.models.dto.UserDto;

import java.util.List;

public interface IUserService {
    public boolean userIsExist(String username);
    public User saveUser(User user) throws SendByOpException;
    public User updateUser(User user) throws SendByOpException;
    public User findByEmail(String email) throws SendByOpException;
    public void deleteuser(User user);
    public List<UserDto> getAllUser();
}
