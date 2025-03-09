package com.sendByOP.expedition.mappers;

import com.sendByOP.expedition.models.dto.NewsletterDto;
import com.sendByOP.expedition.models.entities.Newsletter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NewsletterMapper {

    NewsletterMapper INSTANCE = Mappers.getMapper(NewsletterMapper.class);

    NewsletterDto toDto(Newsletter newsletter);

    Newsletter toEntity(NewsletterDto newsletterDto);
}