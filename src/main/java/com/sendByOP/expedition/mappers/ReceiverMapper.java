package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.ReceiverDto;
import com.sendByOP.expedition.models.entities.Receiver;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReceiverMapper {

    ReceiverMapper INSTANCE = Mappers.getMapper(ReceiverMapper.class);

    ReceiverDto toDto(Receiver receiver);

    Receiver toEntity(ReceiverDto receiverDto);
}
