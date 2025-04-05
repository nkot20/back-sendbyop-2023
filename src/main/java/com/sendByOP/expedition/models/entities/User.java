package com.sendByOP.expedition.models.entities;

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
    @Basic(optional = false)
    @Column(name = "username")
    private String username;

    @Basic(optional = false)
    @Column(name = "email")
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

    @JoinColumn(name = "role", referencedColumnName = "idRole")
    @ManyToOne(optional = false)
    private Role role;
}
