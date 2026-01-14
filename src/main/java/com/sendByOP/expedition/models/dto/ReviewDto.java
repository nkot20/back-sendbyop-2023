package com.sendByOP.expedition.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ReviewDto {
    private int id;
    private String rating;
    private String opinion;
    private Date date;
    private Integer transporterId;
    private Integer shipperId;
    private Integer bookingId; // ID de la réservation (optionnel)
    
    // Informations supplémentaires pour l'affichage
    private String reviewerName; // Nom du client qui a laissé l'avis
    private String travelerId; // ID du voyageur noté
    private String flightInfo; // Info du vol (départ -> arrivée)
    
    // Réponse du voyageur
    private String response;
    private Date responseDate;
}