package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "DTO for booking information")
public class BookingDto implements Serializable {
    @Schema(description = "Unique identifier for the booking", example = "1")
    private Integer id;

    @NotNull(message = "Booking date cannot be null")
    @Schema(description = "Date when the booking was made", example = "2023-12-25")
    private Date bookingDate;

    @Schema(description = "Time of the booking", example = "14:30")
    private String bookingTime;

    @Schema(description = "Payment status of the booking (0: Pending, 1: Paid, 2: Failed)", example = "1")
    private Integer paymentStatus;

    @Schema(description = "Status of the expedition (0: Pending, 1: In Progress, 2: Completed)", example = "1")
    private Integer expeditionStatus;

    @Schema(description = "Review provided by the customer", example = "Great service!")
    private String customerReview;

    @Schema(description = "Review provided by the sender", example = "Excellent handling")
    private String senderReview;

    @Schema(description = "Reception status from sender's side (0: Pending, 1: Received)", example = "1")
    private int senderReceptionStatus;

    @Schema(description = "Reception status from customer's side (0: Pending, 1: Received)", example = "1")
    private int customerReceptionStatus;

    @NotNull(message = "Receiver ID cannot be null")
    @Schema(description = "ID of the receiver", example = "1")
    private Integer receiverId;

    @NotNull(message = "Flight ID cannot be null")
    @Schema(description = "ID of the associated flight", example = "1")
    private Integer flightId;

    @NotNull(message = "Customer ID cannot be null")
    @Schema(description = "ID of the customer who made the booking", example = "1")
    private Integer customerId;

    @Schema(description = "Booking cancellation status (0: Active, 1: Cancelled)", example = "0")
    private int cancelled;

    @Schema(description = "Payment status for the transporter (0: Pending, 1: Paid)", example = "1")
    private int transporterPaymentStatus;

    @Schema(description = "List of parcels associated with this booking")
    private List<ParcelDto> parcelIds;

}
