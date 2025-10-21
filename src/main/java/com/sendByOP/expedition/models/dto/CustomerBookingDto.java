package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for customer booking information with flight and parcel details")
public class CustomerBookingDto implements Serializable {
    
    @Schema(description = "Unique identifier for the booking", example = "1")
    private Integer id;

    @Schema(description = "Date when the booking was made", example = "2023-12-25")
    private Date bookingDate;

    @Schema(description = "Time of the booking", example = "14:30")
    private String bookingTime;

    @Schema(description = "Payment status (0: Pending, 1: Paid, 2: Failed)", example = "1")
    private Integer paymentStatus;

    @Schema(description = "Expedition status (0: Pending, 1: In Progress, 2: Completed, 3: Delivered)", example = "1")
    private Integer expeditionStatus;

    @Schema(description = "Booking cancellation status (0: Active, 1: Cancelled)", example = "0")
    private int cancelled;

    @Schema(description = "Customer reception status (0: Pending, 1: Received)", example = "1")
    private int customerReceptionStatus;

    @Schema(description = "Sender reception status (0: Pending, 1: Received)", example = "1")
    private int senderReceptionStatus;

    @Schema(description = "Review provided by the customer", example = "Great service!")
    private String customerReview;

    @Schema(description = "Review provided by the sender", example = "Excellent handling")
    private String senderReview;

    // Flight information
    @Schema(description = "Flight details")
    private FlightSummaryDto flight;

    // Receiver information
    @Schema(description = "Receiver details")
    private ReceiverDto receiver;

    // Parcels information
    @Schema(description = "List of parcels in this booking")
    private List<ParcelDto> parcels;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Summary of flight information for booking")
    public static class FlightSummaryDto implements Serializable {
        @Schema(description = "Flight ID", example = "1")
        private Integer id;

        @Schema(description = "Departure date", example = "2023-12-25")
        private Date departureDate;

        @Schema(description = "Departure time", example = "14:30")
        private String departureTime;

        @Schema(description = "Arrival date", example = "2023-12-25")
        private Date arrivalDate;

        @Schema(description = "Arrival time", example = "18:45")
        private String arrivalTime;

        @Schema(description = "Available weight in kg", example = "15.5")
        private Double availableWeight;

        @Schema(description = "Price per kg", example = "25.0")
        private Double pricePerKg;

        @Schema(description = "Departure airport name", example = "Charles de Gaulle Airport")
        private String departureAirportName;

        @Schema(description = "Departure airport code", example = "CDG")
        private String departureAirportCode;

        @Schema(description = "Departure city", example = "Paris")
        private String departureCityName;

        @Schema(description = "Departure country", example = "France")
        private String departureCountryName;

        @Schema(description = "Arrival airport name", example = "John F. Kennedy International Airport")
        private String arrivalAirportName;

        @Schema(description = "Arrival airport code", example = "JFK")
        private String arrivalAirportCode;

        @Schema(description = "Arrival city", example = "New York")
        private String arrivalCityName;

        @Schema(description = "Arrival country", example = "United States")
        private String arrivalCountryName;

        @Schema(description = "Traveler first name", example = "John")
        private String travelerFirstName;

        @Schema(description = "Traveler last name", example = "Doe")
        private String travelerLastName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Receiver information")
    public static class ReceiverDto implements Serializable {
        @Schema(description = "Receiver ID", example = "1")
        private Integer id;

        @Schema(description = "Receiver first name", example = "Jane")
        private String firstName;

        @Schema(description = "Receiver last name", example = "Smith")
        private String lastName;

        @Schema(description = "Receiver phone number", example = "+1234567890")
        private String phoneNumber;

        @Schema(description = "Receiver email", example = "jane.smith@example.com")
        private String email;

        @Schema(description = "Receiver address", example = "123 Main St, New York, NY")
        private String address;
    }
}
