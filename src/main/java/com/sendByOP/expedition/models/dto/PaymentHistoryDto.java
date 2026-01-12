package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour l'historique des paiements avec informations détaillées
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Historique de paiement détaillé")
public class PaymentHistoryDto implements Serializable {

    @Schema(description = "ID du paiement", example = "1")
    private Integer id;

    @Schema(description = "Date du paiement")
    private LocalDateTime paymentDate;

    @Schema(description = "Montant du paiement", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "Méthode de paiement", example = "Carte bancaire")
    private String paymentMethod;

    @Schema(description = "Statut du paiement", example = "COMPLETED")
    private String status;

    @Schema(description = "Libellé du statut", example = "Complété")
    private String statusDisplayName;

    @Schema(description = "Description du paiement", example = "Paiement réservation #123")
    private String description;

    @Schema(description = "Référence de transaction", example = "TXN-2024-001234")
    private String transactionReference;

    // Informations sur la réservation associée
    @Schema(description = "ID de la réservation associée")
    private Integer bookingId;

    @Schema(description = "Résumé du vol associé")
    private FlightSummaryDto flight;

    @Schema(description = "Nom du destinataire")
    private String receiverName;

    @Schema(description = "Poids du colis en kg")
    private Double parcelWeight;

    /**
     * DTO résumé du vol pour l'historique des paiements
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Résumé du vol pour l'historique des paiements")
    public static class FlightSummaryDto implements Serializable {
        @Schema(description = "ID du vol")
        private Integer flightId;

        @Schema(description = "Ville de départ", example = "Paris")
        private String departureCityName;

        @Schema(description = "Code aéroport de départ", example = "CDG")
        private String departureAirportCode;

        @Schema(description = "Ville d'arrivée", example = "Douala")
        private String arrivalCityName;

        @Schema(description = "Code aéroport d'arrivée", example = "DLA")
        private String arrivalAirportCode;

        @Schema(description = "Date de départ")
        private LocalDateTime departureDate;
    }

    /**
     * Statuts possibles d'un paiement
     */
    public enum PaymentStatus {
        PENDING("En attente"),
        PROCESSING("En cours de traitement"),
        COMPLETED("Complété"),
        FAILED("Échoué"),
        REFUNDED("Remboursé"),
        CANCELLED("Annulé");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
