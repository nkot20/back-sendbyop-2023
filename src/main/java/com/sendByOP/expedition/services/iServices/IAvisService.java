package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.ReviewDto;
import com.sendByOP.expedition.models.dto.CustomerDto;

import java.util.List;

public interface IAvisService {
    ReviewDto saveOpinion(ReviewDto avis);
    List<ReviewDto> getByTransporter(int transporterId);
    List<ReviewDto> getByExpeditor(int expeditorId);
    public List<ReviewDto> findByTransporteur(CustomerDto transporteur);
}