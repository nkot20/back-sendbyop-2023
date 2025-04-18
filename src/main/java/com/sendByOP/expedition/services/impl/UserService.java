package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.dto.UserDto;
import com.sendByOP.expedition.services.iServices.IUserService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.repositories.UserRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SendMailService sendMailService;

    @Override
    public boolean userIsExist(String username) {
        log.debug("Checking if user exists with email: {}", username);
        return userRepository.existsByEmail(username);
    }

    @Override
    public User saveUser(User user) throws SendByOpException {
        log.debug("Saving new user with email: {}", user.getEmail());
        CHeckNull.checkEmail(user.getEmail());
        CHeckNull.checkLibelle(user.getPassword());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) throws SendByOpException {
        log.debug("Updating user with email: {}", user.getEmail());
        CHeckNull.checkEmail(user.getEmail());
        CHeckNull.checkLibelle(user.getPassword());
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) throws SendByOpException {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.USER_NOT_FOUND));
    }

    @Override
    public void deleteuser(User user) {
        log.debug("Deleting user with email: {}", user.getEmail());
        userRepository.delete(user);
    }

    @Override
    public List<UserDto> getAllUser() {
        log.debug("Retrieving all users");
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUserPassword(String email, String newPassword) throws SendByOpException {
        log.debug("Updating password for user with email: {}", email);
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new SendByOpException(ErrorInfo.INVALID_PASSWORD);
        }

        User user = findByEmail(email);
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SendByOpException(ErrorInfo.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = updateUser(user);

        sendPasswordUpdateEmail(email);
        
        return convertToDto(updatedUser);
    }

    private void sendPasswordUpdateEmail(String email) {
        EmailDto emailDto = EmailDto.builder()
                .to(email)
                .body("Your password has been successfully updated!")
                .topic("Password Update Confirmation")
                .build();
        sendMailService.sendEmail(emailDto);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roleId(user.getRole().getId())
                .build();
    }
}
