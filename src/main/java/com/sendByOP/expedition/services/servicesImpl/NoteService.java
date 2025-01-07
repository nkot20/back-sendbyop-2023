package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Note;
import com.sendByOP.expedition.repositories.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class NoteService {

    @Autowired
    NoteRepository noteRepository;

    public Note saveNote(Note note){
        return noteRepository.save(note);
    }

    public List<Note> getNoteOfExpedi(int id){
        return noteRepository.findByIdexp(id);
    }
}
