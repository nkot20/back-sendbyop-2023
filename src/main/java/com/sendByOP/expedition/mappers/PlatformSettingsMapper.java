package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.PlatformSettingsDto;
import com.sendByOP.expedition.models.entities.PlatformSettings;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Mapper pour PlatformSettings
 */
@Mapper(componentModel = "spring")
public interface PlatformSettingsMapper {
    
    PlatformSettingsMapper INSTANCE = Mappers.getMapper(PlatformSettingsMapper.class);
    
    /**
     * Convertit une entité en DTO
     */
    PlatformSettingsDto toDto(PlatformSettings entity);
    
    /**
     * Convertit un DTO en entité
     */
    PlatformSettings toEntity(PlatformSettingsDto dto);
    
    /**
     * Met à jour une entité existante avec les données d'un DTO
     * Ignore les champs null du DTO
     */
    void updateEntityFromDto(PlatformSettingsDto dto, @MappingTarget PlatformSettings entity);
}
