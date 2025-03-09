package com.sendByOP.expedition.models.dto;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class BankInfoDto {
    private Integer id;
    private Integer clientId;
    private String iban;
    private String countryName;
    private String bankAccount;
    private String bankName;
    private String bic;
    private String accountHolder;
}
