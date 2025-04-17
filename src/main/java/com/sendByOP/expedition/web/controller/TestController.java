package com.sendByOP.expedition.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final PasswordEncoder passwordEncoder;
    @PostMapping("/api/v1/test/{pw}")
    public ResponseEntity<String> pwTest(@PathVariable("pw") String pw) {
        String pwd = passwordEncoder.encode(pw);
        System.out.println(pwd);
       return new ResponseEntity<>(pwd, HttpStatus.CREATED);
    }

}
