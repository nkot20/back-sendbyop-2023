package com.sendByOP.expedition.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class testpw {

    @Autowired(required = true)
    public static PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        System.out.println(passwordEncoder.encode("sendbyop@2025"));
    }

}
