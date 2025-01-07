package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NoteDto {
    private Integer idNote;
    private int nb;
    private Integer idExpe;  // Id du client expéditeur
    private Integer idClient;  // Id du client
}
