package com.sendByOP.expedition.models.dto;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ReceveurDto implements Serializable {

    private Integer idre;
    private String nom;
    private String prenom;
    private String tel;
    private String email;

}
