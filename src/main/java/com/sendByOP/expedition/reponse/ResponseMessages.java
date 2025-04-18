package com.sendByOP.expedition.reponse;

public enum ResponseMessages {
    USER_REGISTERED_SUCCESSFULLY("User registered successfully!"),
    PASSWORD_UPDATED_SUCCESSFULLY("Password updated successfully!"),
    USER_DELETED_SUCCESSFULLY("User deleted successfully!"),
    PROBLEM_OCCURRED("Un problème est survenu, veuillez réessayer plus tard"),
    RESERVATION_NOT_FOUND("Réservation introuvable"),
    NO_OPINION_FOR_RESERVATION("Aucun avis pour cette réservation"),
    TRANSPORTER_NOT_FOUND("Transporteur introuvable"),
    NO_OPINIONS_FOR_TRANSPORTER("Aucun avis pour ce transporteur"),
    NO_OPINIONS_FOR_EXPEDITOR("Aucun avis pour cet expéditeur"),
    OPERATION_SAVED_SUCCESSFULLY("Operation saved successfully!"),
    RESERVATION_SAVED_SUCCESSFULLY("Reservation saved successfully!"),
    OPERATION_DELETED_SUCCESSFULLY("Operation deleted successfully!"),
    OPERATION_NOT_FOUND("Operation not found."),
    TYPE_OPERATION_NOT_FOUND("Type of operation not found."),
    NO_PAYMENTS_FOUND("Aucun paiement trouvé."),
    CUSTOMER_NOT_FOUND("Client introuvable."),
    PAYMENT_ERROR("Une erreur est survenue lors du paiement."),
    OPINION_SAVED_SUCCESSFULLY("Opinion saved successfully!");


    private final String message;

    ResponseMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
