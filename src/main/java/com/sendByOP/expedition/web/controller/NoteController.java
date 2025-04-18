package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.RatingDto;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.impl.NoteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping(value = "/save")
    public ResponseEntity<?> addNote(@RequestBody RatingDto note){
        RatingDto newNote = noteService.saveNote(note);

        if( newNote == null ){
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu!"),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<RatingDto>(newNote,
                HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getNote(@PathVariable int id){
        List<RatingDto> notes = noteService.getNoteOfExpedi(id);

        return new ResponseEntity<>(notes,
                HttpStatus.OK);
    }

}
