package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.OperationTypeDto;
import com.sendByOP.expedition.models.entities.OperationType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OperationTypeMapper {

    OperationTypeMapper INSTANCE = Mappers.getMapper(OperationTypeMapper.class);

    OperationTypeDto toDto(OperationType operationType);

    OperationType toEntity(OperationTypeDto operationTypeDto);
}
