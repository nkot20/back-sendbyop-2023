package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "newsletter")
public class Newsletter extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_news", nullable = false)
    private Integer id;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

}