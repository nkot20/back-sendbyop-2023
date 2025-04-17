package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.BankInfoDto;
import com.sendByOP.expedition.models.entities.BankInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankInfoMapper {

    @Mapping(source = "customer.id", target = "clientId")
    BankInfoDto toDto(BankInfo bankInfo);

    @Mapping(source = "clientId", target = "customer.id")
    BankInfo toEntity(BankInfoDto bankInfoDTO);
}
