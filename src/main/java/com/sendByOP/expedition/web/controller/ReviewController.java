package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.ReviewDto;
import com.sendByOP.expedition.reponse.ResponseMessages;
import com.sendByOP.expedition.services.iServices.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "API endpoints for managing reviews")
public class ReviewController {
    private final IReviewService reviewService;

    @Operation(summary = "Create a new review", description = "Creates a new review for a transporter or shipper")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created successfully",
                content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<?> saveReview(@Valid @RequestBody @Parameter(description = "Review details") ReviewDto reviewDto) {
        ReviewDto savedReview = reviewService.saveReview(reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }

    @Operation(summary = "Get reviews by transporter", description = "Retrieves all reviews for a specific transporter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews found",
                content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "404", description = "No reviews found")
    })
    @GetMapping("/transporter/{id}")
    public ResponseEntity<?> getByTransporter(@Parameter(description = "Transporter ID") @PathVariable("id") int transporterId) {
        List<ReviewDto> reviews = reviewService.getByTransporter(transporterId);
        if (reviews.isEmpty()) {
            return ResponseEntity.ok(ResponseMessages.NO_OPINIONS_FOR_TRANSPORTER.getMessage());
        }
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get reviews by shipper", description = "Retrieves all reviews for a specific shipper")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews found",
                content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "404", description = "No reviews found")
    })
    @GetMapping("/shipper/{id}")
    public ResponseEntity<?> getByShipper(@Parameter(description = "Shipper ID") @PathVariable("id") int shipperId) {
        List<ReviewDto> reviews = reviewService.getByExpeditor(shipperId);
        if (reviews.isEmpty()) {
            return ResponseEntity.ok(ResponseMessages.NO_OPINIONS_FOR_EXPEDITOR.getMessage());
        }
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Submit a review for a booking", 
               description = "Allows a customer to review a booking/trip after delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review submitted successfully",
                content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or booking not eligible for review"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<?> submitBookingReview(
            @Parameter(description = "Booking ID") @PathVariable("bookingId") Integer bookingId,
            @Valid @RequestBody @Parameter(description = "Review details") ReviewDto reviewDto) {
        reviewDto.setBookingId(bookingId);
        ReviewDto savedReview = reviewService.saveBookingReview(reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }

    @Operation(summary = "Get reviews for a traveler", 
               description = "Retrieves all reviews received by a traveler from booking reviews")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews found",
                content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "404", description = "No reviews found")
    })
    @GetMapping("/traveler/{travelerId}")
    public ResponseEntity<?> getTravelerReviews(
            @Parameter(description = "Traveler (Customer) ID") @PathVariable("travelerId") Integer travelerId) {
        List<ReviewDto> reviews = reviewService.getTravelerReviews(travelerId);
        if (reviews.isEmpty()) {
            return ResponseEntity.ok("Aucun avis pour ce voyageur");
        }
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get reviews given by a customer", 
               description = "Retrieves all reviews that a customer has left on their bookings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews found",
                content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "404", description = "No reviews found")
    })
    @GetMapping("/customer/{customerId}/given")
    public ResponseEntity<?> getCustomerGivenReviews(
            @Parameter(description = "Customer ID") @PathVariable("customerId") Integer customerId) {
        List<ReviewDto> reviews = reviewService.getCustomerGivenReviews(customerId);
        if (reviews.isEmpty()) {
            return ResponseEntity.ok("Aucun avis donné par ce client");
        }
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Respond to a review", 
               description = "Allows a traveler to respond to a review they received")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Response added successfully",
                content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Not authorized to respond to this review"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PutMapping("/{reviewId}/respond")
    public ResponseEntity<?> respondToReview(
            @Parameter(description = "Review ID") @PathVariable("reviewId") Integer reviewId,
            @Parameter(description = "Traveler ID") @RequestParam("travelerId") Integer travelerId,
            @Parameter(description = "Response text") @RequestBody String responseText) {
        ReviewDto updatedReview = reviewService.respondToReview(reviewId, responseText, travelerId);
        return ResponseEntity.ok(updatedReview);
    }
}
