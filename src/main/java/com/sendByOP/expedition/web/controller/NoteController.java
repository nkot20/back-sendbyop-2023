package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Rating;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    NoteService noteService;

    @PostMapping(value = "/save")
    public ResponseEntity<?> addNote(@RequestBody Rating note){
        Rating newNote = noteService.saveNote(note);

        if( newNote == null ){
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu!"),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Rating>(newNote,
                HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getNote(@PathVariable int id){
        List<Rating> notes = noteService.getNoteOfExpedi(id);

        return new ResponseEntity<>(notes,
                HttpStatus.OK);
    }

}
