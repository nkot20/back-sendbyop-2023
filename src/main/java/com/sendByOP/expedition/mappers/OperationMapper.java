package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.OperationDto;
import com.sendByOP.expedition.models.entities.Operation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OperationMapper {

    OperationMapper INSTANCE = Mappers.getMapper(OperationMapper.class);

    @Mapping(source = "operationType.id", target = "operationTypeId")
    @Mapping(source = "reservation.id", target = "reservationId")
    OperationDto toDto(Operation operation);

    @Mapping(source = "operationTypeId", target = "operationType.id")
    @Mapping(source = "reservationId", target = "reservation.id")
    Operation toEntity(OperationDto operationDto);
}