package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.InvoiceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FactureMapper {

    FactureMapper INSTANCE = Mappers.getMapper(FactureMapper.class);

    @Mapping(source = "idfac", target = "idfac")
    @Mapping(source = "montantfac", target = "montantfac")
    @Mapping(source = "datepaie", target = "datepaie")
    @Mapping(source = "idRe", target = "idRe")
    InvoiceDto toDto(com.sendByOP.expedition.models.entities.Invoice facture);

    @Mapping(source = "idfac", target = "idfac")
    @Mapping(source = "montantfac", target = "montantfac")
    @Mapping(source = "datepaie", target = "datepaie")
    @Mapping(source = "idRe", target = "idRe")
    com.sendByOP.expedition.models.entities.Invoice toEntity(InvoiceDto factureDto);
}