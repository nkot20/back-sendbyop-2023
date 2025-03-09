package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.PaymentTypeDto;
import com.sendByOP.expedition.models.entities.PaymentType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentTypeMapper {
    PaymentTypeDto toDto(PaymentType paymentType);
    PaymentType toEntity(PaymentTypeDto paymentTypeDTO);
}
