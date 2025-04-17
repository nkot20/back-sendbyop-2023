package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.message.LoginForm;
import com.sendByOP.expedition.message.SignUpForm;
import com.sendByOP.expedition.reponse.JwtResponse;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.models.entities.User;

public interface IAuthService {
    JwtResponse authenticateUser(LoginForm loginRequest) throws SendByOpException;
    ResponseMessage registerUser(SignUpForm signUpRequest) throws SendByOpException;
    ResponseMessage changePassword(String email, String oldPw, String newPw) throws SendByOpException;
    ResponseMessage deleteUser(String email) throws SendByOpException;
    User updateUser(User user) throws SendByOpException;
    User getUserByEmail(String email) throws SendByOpException;
}
