package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Basic(optional = false)
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Basic(optional = false)
    @Column(name = "password")
    private String password; // Renamed from 'pw' to 'password'

    @Basic(optional = false)
    @Column(name = "last_name")
    private String lastName; // Renamed from 'nom' to 'lastName'

    @Basic(optional = false)
    @Column(name = "first_name")
    private String firstName; // Renamed from 'prenom' to 'firstName'

    @Column(name = "role")
    private String role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;
    
    @Builder.Default
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;
    
    @Column(name = "otp_secret")
    private String otpSecret;
    
    @Column(name = "otp_sent_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date otpSentAt;
}
