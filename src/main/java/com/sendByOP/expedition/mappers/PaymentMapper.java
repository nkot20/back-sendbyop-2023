package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.PaymentDto;
import com.sendByOP.expedition.models.entities.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "paymentType.paymentTypeId", target = "paymentTypeId")
    PaymentDto toDto(Payment payment);

    @Mapping(source = "clientId", target = "client.id")
    @Mapping(source = "paymentTypeId", target = "paymentType.paymentTypeId")
    Payment toEntity(PaymentDto paymentDTO);
}
