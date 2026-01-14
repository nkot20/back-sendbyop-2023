package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.ReceiverDto;
import com.sendByOP.expedition.models.entities.Receiver;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReceiverMapper {

    ReceiverDto toDto(Receiver receiver);

    Receiver toEntity(ReceiverDto receiverDto);
}
