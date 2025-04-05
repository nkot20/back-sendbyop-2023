package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.entities.Customer;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerMapper {
    @Mapping(source = "clientId", target = "customer.id")
    Customer toEntity(CustomerDto clientDto);

    @Mapping(source = "customer.id", target = "clientId")
    CustomerDto toDto(Customer client);

    void copy(CustomerDto clientDto, @MappingTarget Customer client);
}
