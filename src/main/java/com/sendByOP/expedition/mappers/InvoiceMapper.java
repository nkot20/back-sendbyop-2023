package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.InvoiceDto;
import com.sendByOP.expedition.models.entities.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InvoiceMapper {

    InvoiceMapper INSTANCE = Mappers.getMapper(InvoiceMapper.class);

    @Mapping(source = "reservation.id", target = "reservationId")
    InvoiceDto toDto(Invoice invoice);

    @Mapping(source = "reservationId", target = "reservation.id")
    Invoice toEntity(InvoiceDto invoiceDto);
}
