package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.RatingDto;

import java.util.List;

public interface INoteService {
    public RatingDto saveNote(RatingDto note);

    public List<RatingDto> getNoteOfExpedi(int id);
}
