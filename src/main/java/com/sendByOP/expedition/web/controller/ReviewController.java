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
}
