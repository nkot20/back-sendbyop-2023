package com.sendByOP.expedition.models.dto;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InfoBancaireDto {
    private Integer idInfo;
    private Integer idClient; // ID du client au lieu de l'objet `Client`
    private String iban;
    private String countryName;
    private String bankAccount;
    private String bankName;
    private String bic;
    private String accountHolder;
}
