package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ReceiverDto implements Serializable {

    private Integer id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;

}
