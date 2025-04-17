package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.InvoiceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FactureMapper {

    FactureMapper INSTANCE = Mappers.getMapper(FactureMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "paymentDate", target = "paymentDate")
    @Mapping(source = "reservation.id", target = "reservationId")
    InvoiceDto toDto(com.sendByOP.expedition.models.entities.Invoice facture);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "paymentDate", target = "paymentDate")
    @Mapping(source = "reservationId", target = "reservation.id")
    com.sendByOP.expedition.models.entities.Invoice toEntity(InvoiceDto factureDto);
}