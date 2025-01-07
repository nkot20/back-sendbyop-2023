package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
@Builder
public class PaiementDto {
    private Integer idPaiement;
    private Date date;
    private Integer clientId;  // Id du client
    private Double montant;
    private Integer typePaimentId;  // Id du type de paiement
}
