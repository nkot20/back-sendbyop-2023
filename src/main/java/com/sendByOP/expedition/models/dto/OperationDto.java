package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class OperationDto {
    private Integer idOpe;
    private Date date;
    private Integer idTypeOperation;  // Id du type d'opération
    private Integer idReser;  // Id de la réservation
}
