package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.security.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "bank_info")
public class BankInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_info", nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_client", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @Column(name = "iban", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String iban;

    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "bank_account", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String bankAccount;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bic", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String bic;

    @Column(name = "account_holder", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String accountHolder;
}
