package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.model.Note;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NoteController {

    @Autowired
    NoteService noteService;

    @PostMapping(value = "note/add")
    public ResponseEntity<?> addNote(@RequestBody Note note){
        Note newNote = noteService.saveNote(note);

        if( newNote == null ){
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu!"),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Note>(newNote,
                HttpStatus.OK);
    }

    @GetMapping(value = "note/{id}")
    public ResponseEntity<?> getNote(@PathVariable int id){
        List<Note> notes = noteService.getNoteOfExpedi(id);

        return new ResponseEntity<>(notes,
                HttpStatus.OK);
    }

}
