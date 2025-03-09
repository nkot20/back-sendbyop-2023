package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "customer_id")
    private Integer id;

    @Column(name = "national_id_number")
    private Integer nationalIdNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "birth_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthDate;

    @Column(name = "identity_document")
    private String identityDocument;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "password")
    private String password;

    @Column(name = "registration_status")
    private int registrationStatus;

    @Column(name = "email_verified")
    private int emailVerified;

    @Column(name = "phone_verified")
    private int phoneVerified;

    @Column(name = "whatsapp_link")
    private String whatsappLink;

    @Column(name = "registration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date registrationDate;

    @Column(name = "country")
    private String country;

    @Column(name = "identity_uploaded", nullable = false)
    private int identityUploaded;

    @Column(name = "identity_verified")
    private int identityVerified;

    @Column(name = "iban")
    private String iban;

    @Column(name = "address")
    private String address;
}
