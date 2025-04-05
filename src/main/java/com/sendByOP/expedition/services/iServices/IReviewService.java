package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.dto.ReviewDto;
import com.sendByOP.expedition.models.dto.CustomerDto;

import java.util.List;

public interface IReviewService {
    ReviewDto saveReview(ReviewDto review);
    List<ReviewDto> getByTransporter(int transporterId);
    List<ReviewDto> getByExpeditor(int expeditorId);
    List<ReviewDto> findByTransporter(CustomerDto transporter);
}