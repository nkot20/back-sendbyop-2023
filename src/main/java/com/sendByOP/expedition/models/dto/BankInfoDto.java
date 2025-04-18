package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Schema(description = "DTO for bank account information")
public class BankInfoDto {
    @Schema(description = "Unique identifier for the bank information", example = "1")
    private Integer id;

    @NotNull(message = "Client ID cannot be null")
    @Schema(description = "ID of the client associated with the bank account", example = "1")
    private Integer clientId;

    @NotNull(message = "IBAN cannot be null")
    @Schema(description = "International Bank Account Number", example = "DE89370400440532013000")
    private String iban;

    @Schema(description = "Name of the country where the bank account is located", example = "Germany")
    private String countryName;

    @NotNull(message = "Bank account number cannot be null")
    @Schema(description = "Bank account number", example = "1234567890")
    private String bankAccount;

    @NotNull(message = "Bank name cannot be null")
    @Schema(description = "Name of the bank", example = "Deutsche Bank")
    private String bankName;

    @Schema(description = "Bank Identifier Code (SWIFT)", example = "DEUTDEFF")
    private String bic;

    @NotNull(message = "Account holder name cannot be null")
    @Schema(description = "Name of the account holder", example = "John Doe")
    private String accountHolder;
}
