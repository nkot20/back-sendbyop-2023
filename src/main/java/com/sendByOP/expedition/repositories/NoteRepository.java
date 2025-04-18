package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Rating, Integer> {

    @Query("SELECT n FROM Rating n WHERE n.sender.id = :id")
    List<Rating> findBySender(int id);
}
