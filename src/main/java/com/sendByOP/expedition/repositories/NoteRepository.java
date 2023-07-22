package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {

    List<Note> findByIdexp(int idexp);
}
