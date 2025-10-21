package com.sendByOP.expedition.models.entities;

import com.sendByOP.expedition.models.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "user")
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
}
