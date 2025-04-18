package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.RoleDto;
import com.sendByOP.expedition.models.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    RoleDto toDto(Role role);

    Role toEntity(RoleDto roleDto);
}