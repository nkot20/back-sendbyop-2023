package com.sendByOP.expedition.models.dto;

import lombok.*;

/**
 * DTO pour les photos de colis
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelPhotoDto {
    
    private Integer id;
    private String photoUrl;
    private String description;
    private Integer displayOrder;
    private Boolean isPrimary;
}
