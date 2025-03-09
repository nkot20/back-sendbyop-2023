package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.NewsletterDto;
import com.sendByOP.expedition.models.entities.Newsletter;
import com.sendByOP.expedition.reponse.ResponseMessages;

import java.util.List;
import java.util.Optional;

public interface INewsLetter {
    public NewsletterDto save(NewsletterDto newsletter) throws SendByOpException;

    public List<NewsletterDto> getAll();

    public Optional<NewsletterDto> getNewsLetterByEmail(String email);
}
