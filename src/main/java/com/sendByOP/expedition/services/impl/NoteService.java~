package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.RatingMapper;
import com.sendByOP.expedition.models.dto.RatingDto;
import com.sendByOP.expedition.models.entities.Rating;
import com.sendByOP.expedition.repositories.NoteRepository;
import com.sendByOP.expedition.services.iServices.INoteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NoteService implements INoteService {

    private final NoteRepository noteRepository;
    private final RatingMapper noteMapper;

    @Override
    public RatingDto saveNote(RatingDto noteDto) {
        Rating note = noteMapper.toEntity(noteDto);
        Rating savedNote = noteRepository.save(note);
        return noteMapper.toDto(savedNote);
    }

    @Override
    public List<RatingDto> getNoteOfExpedi(int id) {
        List<Rating> notes = noteRepository.findBySender(id);
        return notes.stream()
                .map(noteMapper::toDto)
                .collect(Collectors.toList());
    }
}