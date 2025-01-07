package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Newsletter;
import com.sendByOP.expedition.repositories.NewsletterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NewslettrerService {

    @Autowired
    public NewsletterRepository newsletterRepository;

    public Newsletter save(Newsletter newsletter) {
        return newsletterRepository.save(newsletter);
    }

    public List<Newsletter> getAll() {
        return newsletterRepository.findAll();
    }

    public Optional<Newsletter> getNewsLetterByEmail(String email) {
        return newsletterRepository.findByEmail(email);
    }

}
