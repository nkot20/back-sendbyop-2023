package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.mappers.ReviewMapper;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.ReviewDto;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.Review;
import com.sendByOP.expedition.repositories.ReviewRepository;
import com.sendByOP.expedition.services.iServices.IReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final CustomerService customerService;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final CustomerMapper customerMapper;

    @Override
    public ReviewDto saveReview(ReviewDto reviewDto) {
        if (reviewDto == null) {
            throw new IllegalArgumentException("Review data cannot be null");
        }

        Review review = reviewMapper.toEntity(reviewDto);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    @Override
    public List<ReviewDto> getByTransporter(int transporterId) {
        CustomerDto transporterDto = customerService.getClientById(transporterId);

        if (transporterDto == null) {
            throw new IllegalArgumentException("Transporter not found");
        }

        Customer transporter = customerMapper.toEntity(transporterDto);
        List<Review> reviewList = reviewRepository.findByTransporteurOrderByDateAsc(transporter);

        return reviewList.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getByExpeditor(int expeditorId) {
        CustomerDto expeditorDto = customerService.getClientById(expeditorId);

        if (expeditorDto == null) {
            throw new IllegalArgumentException("Expeditor not found");
        }

        Customer expeditor = customerMapper.toEntity(expeditorDto);
        List<Review> reviewList = reviewRepository.findByExpediteurOrderByDateAsc(expeditor);

        return reviewList.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> findByTransporter(CustomerDto transporterDto) {
        if (transporterDto == null) {
            throw new IllegalArgumentException("Transporter data cannot be null");
        }

        Customer transporter = customerMapper.toEntity(transporterDto);
        List<Review> reviewList = reviewRepository.findByTransporteurOrderByDateAsc(transporter);

        return reviewList.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
}