package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerMapper {
    Customer toEntity(CustomerDto clientDto);

    CustomerDto toDto(Customer client);

    void copy(CustomerDto clientDto, @MappingTarget Customer client);

    List<CustomerDto> toDtoList(List<Customer> clients);
}
