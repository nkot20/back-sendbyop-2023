package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VolEscaleDto {
    private VolDto vol;
    private List<EscaleDto> escales;
}
