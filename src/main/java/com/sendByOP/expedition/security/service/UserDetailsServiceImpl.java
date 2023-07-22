package com.sendByOP.expedition.security.service;

import com.sendByOP.expedition.model.User;
import com.sendByOP.expedition.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    UserDetails userDetail;
    UserPrinciple userPrinciple;
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails;
        User user = userRepository.findByUsername( username).orElseThrow(
                () -> new UsernameNotFoundException("User Not Found with -> username or email : " + username));
        return  UserPrinciple.build(user);
    }
}
