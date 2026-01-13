package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.mappers.ReviewMapper;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.ReviewDto;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.Review;
import com.sendByOP.expedition.models.enums.BookingStatus;
import com.sendByOP.expedition.repositories.BookingRepository;
import com.sendByOP.expedition.repositories.ReviewRepository;
import com.sendByOP.expedition.services.iServices.IReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final CustomerService customerService;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
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
        List<Review> reviewList = reviewRepository.findByTransporterOrderByDateAsc(transporter);

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
        List<Review> reviewList = reviewRepository.findByShipperOrderByDateAsc(expeditor);

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
        List<Review> reviewList = reviewRepository.findByTransporterOrderByDateAsc(transporter);

        return reviewList.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Soumet un avis pour une réservation
     * Vérifie que la réservation est livrée et que le client peut laisser un avis
     */
    @Override
    public ReviewDto saveBookingReview(ReviewDto reviewDto) throws SendByOpException {
        log.info("Saving review for booking {}", reviewDto.getBookingId());

        if (reviewDto.getBookingId() == null) {
            throw new SendByOpException(ErrorInfo.INVALID_DATA, "Booking ID is required");
        }

        // Récupérer la réservation
        Booking booking = bookingRepository.findById(reviewDto.getBookingId())
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND,
                        "Réservation non trouvée"));

        // Vérifier que la réservation est livrée
        if (booking.getStatus() != BookingStatus.PARCEL_DELIVERED_TO_RECEIVER &&
                booking.getStatus() != BookingStatus.DELIVERED &&
                booking.getStatus() != BookingStatus.CONFIRMED_BY_RECEIVER) {
            throw new SendByOpException(ErrorInfo.INVALID_STATUS,
                    "Vous ne pouvez laisser un avis que pour une réservation livrée");
        }

        // Vérifier qu'il n'y a pas déjà un avis pour cette réservation
        if (reviewRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new SendByOpException(ErrorInfo.DUPLICATE_ENTRY,
                    "Vous avez déjà laissé un avis pour cette réservation");
        }

        // Créer l'avis
        Review review = new Review();
        review.setRating(reviewDto.getRating());
        review.setOpinion(reviewDto.getOpinion());
        review.setDate(new Date());
        review.setBooking(booking);
        review.setShipper(booking.getCustomer()); // Client qui laisse l'avis
        review.setTransporter(booking.getFlight().getCustomer()); // Voyageur qui est noté

        Review savedReview = reviewRepository.save(review);

        log.info("Review saved successfully for booking {}", booking.getId());

        return convertToReviewDto(savedReview);
    }

    /**
     * Récupère tous les avis reçus par un voyageur
     */
    @Override
    public List<ReviewDto> getTravelerReviews(Integer travelerId) {
        log.info("Getting reviews for traveler {}", travelerId);

        List<Review> reviews = reviewRepository.findByTransporterIdAndBookingIsNotNull(travelerId);

        return reviews.stream()
                .map(this::convertToReviewDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les avis donnés par un client sur ses réservations
     */
    @Override
    public List<ReviewDto> getCustomerGivenReviews(Integer customerId) {
        log.info("Getting reviews given by customer {}", customerId);

        List<Review> reviews = reviewRepository.findByShipperIdAndBookingIsNotNull(customerId);

        return reviews.stream()
                .map(this::convertToReviewDto)
                .collect(Collectors.toList());
    }

    /**
     * Permet au voyageur de répondre à un avis
     */
    @Override
    public ReviewDto respondToReview(Integer reviewId, String responseText, Integer travelerId) throws SendByOpException {
        log.info("Traveler {} responding to review {}", travelerId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND, "Avis non trouvé"));

        // Vérifier que le voyageur est bien le destinataire de cet avis
        if (!review.getTransporter().getId().equals(travelerId)) {
            throw new SendByOpException(ErrorInfo.UNAUTHORIZED,
                    "Vous ne pouvez répondre qu'aux avis qui vous sont adressés");
        }

        // Enregistrer la réponse
        review.setResponse(responseText);
        review.setResponseDate(new Date());

        Review savedReview = reviewRepository.save(review);

        log.info("Response saved for review {}", reviewId);

        return convertToReviewDto(savedReview);
    }

    /**
     * Convertit une entité Review en DTO avec informations enrichies
     */
    private ReviewDto convertToReviewDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setOpinion(review.getOpinion());
        dto.setDate(review.getDate());
        dto.setTransporterId(review.getTransporter().getId());
        dto.setShipperId(review.getShipper().getId());
        dto.setResponse(review.getResponse());
        dto.setResponseDate(review.getResponseDate());

        if (review.getBooking() != null) {
            dto.setBookingId(review.getBooking().getId());
            dto.setReviewerName(review.getShipper().getFirstName() + " " + review.getShipper().getLastName());
            dto.setTravelerId(String.valueOf(review.getTransporter().getId())); // Ajouter le travelerId
            dto.setFlightInfo(
                    review.getBooking().getFlight().getDepartureAirport().getCity().getName() + " → " +
                            review.getBooking().getFlight().getArrivalAirport().getCity().getName()
            );
        }

        return dto;
    }
}