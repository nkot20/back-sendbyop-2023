package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.entities.Customer;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerMapper {
    @Mapping(source = "id", target = "id")
    Customer toEntity(CustomerDto customerDto);

    @Mapping(source = "id", target = "id")
    CustomerDto toDto(Customer customer);

    void copy(CustomerDto clientDto, @MappingTarget Customer client);
}
