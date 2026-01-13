package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.ReviewDto;
import com.sendByOP.expedition.models.dto.CustomerDto;

import java.util.List;

public interface IReviewService {
    ReviewDto saveReview(ReviewDto review);
    List<ReviewDto> getByTransporter(int transporterId);
    List<ReviewDto> getByExpeditor(int expeditorId);
    List<ReviewDto> findByTransporter(CustomerDto transporter);
    
    // Nouvelles méthodes pour les avis de réservation
    ReviewDto saveBookingReview(ReviewDto reviewDto) throws SendByOpException;
    List<ReviewDto> getTravelerReviews(Integer travelerId);
    List<ReviewDto> getCustomerGivenReviews(Integer customerId);
    ReviewDto respondToReview(Integer reviewId, String responseText, Integer travelerId) throws SendByOpException;
}