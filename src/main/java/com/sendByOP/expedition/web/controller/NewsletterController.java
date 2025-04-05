package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.entities.Newsletter;
import com.sendByOP.expedition.services.servicesImpl.NewsLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/newletter")
@RequiredArgsConstructor
public class NewsletterController {
    private final NewsLetterService newslettrerService;

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Newsletter newsletter) throws SendByOpException {

        Newsletter newsletter1 = newslettrerService.save(newsletter);

        return new ResponseEntity<>(newsletter1, HttpStatus.CREATED);
    }

    @PostMapping("/")
    public ResponseEntity<?> getAll(@RequestBody Newsletter newsletter) {
        List<Newsletter> newsletters = newslettrerService.getAll();

        return new ResponseEntity<>(newsletters, HttpStatus.OK);
    }

}
