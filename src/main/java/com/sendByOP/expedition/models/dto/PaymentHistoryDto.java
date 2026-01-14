package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour l'historique des paiements
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryDto {
    private Long id;
    private Integer bookingId;
    private Double amount;
    private String paymentType;
    private LocalDateTime paymentDate;
    private String status;
    private String statusDisplayName;
    private String description;
    private String paymentMethod;
    private String transactionReference;
    private String flightNumber;
    private String departureCity;
    private String arrivalCity;
    private String departureDate;
    
    // Informations de vol détaillées
    private FlightInfoDto flight;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightInfoDto {
        private String departureCityName;
        private String departureAirportCode;
        private String arrivalCityName;
        private String arrivalAirportCode;
        private String departureDate;
    }
}
