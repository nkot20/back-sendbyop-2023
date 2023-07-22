package com.sendByOP.expedition.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "newsletter")
public class Newsletter implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_news", nullable = false)
    private Integer id;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

}