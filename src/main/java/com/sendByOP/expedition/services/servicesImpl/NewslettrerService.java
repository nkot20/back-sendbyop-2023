package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.NewsletterMapper;
import com.sendByOP.expedition.models.dto.NewsletterDto;
import com.sendByOP.expedition.models.entities.Newsletter;
import com.sendByOP.expedition.repositories.NewsletterRepository;
import com.sendByOP.expedition.services.iServices.INewsLetter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NewslettrerService implements INewsLetter {

    private final NewsletterRepository newsletterRepository;
    private final NewsletterMapper newsletterMapper;

    @Override
    public NewsletterDto save(NewsletterDto newsletterDto) throws SendByOpException {
        Optional<Newsletter> existingNewsletter = newsletterRepository.findByEmail(newsletterDto.getEmail());

        if (existingNewsletter.isPresent()) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_ALREADY_USED);
        }

        Newsletter newsletter = newsletterMapper.toEntity(newsletterDto);
        Newsletter savedNewsletter = newsletterRepository.save(newsletter);

        if (savedNewsletter == null) {
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }

        return newsletterMapper.toDto(savedNewsletter);
    }

    @Override
    public List<NewsletterDto> getAll() {
        List<Newsletter> newsletters = newsletterRepository.findAll();
        return newsletters.stream()
                .map(newsletterMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<NewsletterDto> getNewsLetterByEmail(String email) {
        Optional<Newsletter> newsletter = newsletterRepository.findByEmail(email);
        return newsletter.map(newsletterMapper::toDto);
    }
}