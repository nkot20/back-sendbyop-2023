package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.KycDocumentDto;
import com.sendByOP.expedition.models.entities.KycDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper pour KycDocument
 */
@Mapper(componentModel = "spring")
public interface KycDocumentMapper {
    
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.email", target = "customerEmail")
    @Mapping(source = "customer", target = "customerName", qualifiedByName = "getCustomerName")
    @Mapping(source = ".", target = "expired", qualifiedByName = "isExpired")
    @Mapping(source = ".", target = "valid", qualifiedByName = "isValid")
    KycDocumentDto toDto(KycDocument entity);
    
    @Named("getCustomerName")
    default String getCustomerName(com.sendByOP.expedition.models.entities.Customer customer) {
        if (customer == null) return null;
        return customer.getFirstName() + " " + customer.getLastName();
    }
    
    @Named("isExpired")
    default boolean isExpired(KycDocument document) {
        return document.isExpired();
    }
    
    @Named("isValid")
    default boolean isValid(KycDocument document) {
        return document.isValid();
    }
}
