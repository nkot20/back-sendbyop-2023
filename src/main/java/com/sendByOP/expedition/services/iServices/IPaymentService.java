package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PaymentDto;
import com.sendByOP.expedition.models.dto.PaymentHistoryDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPaymentService {
    /**
     * Calculates the invoice amount for a booking
     * @param bookingId The ID of the booking
     * @return Updated booking with payment status
     * @throws SendByOpException if booking not found or calculation fails
     */
    BookingDto calculateInvoiceAmount(int bookingId) throws SendByOpException;

    /**
     * Saves a new payment
     * @param payment The payment to save
     * @return Saved payment with generated ID
     */
    PaymentDto save(PaymentDto payment);

    /**
     * Retrieves all payments
     * @return List of all payments
     */
    List<PaymentDto> getAll();

    /**
     * Retrieves all payments for a specific client
     * @param email Client's email address
     * @return List of payments for the client
     * @throws SendByOpException if client not found
     */
    List<PaymentDto> getPaymentsByClient(String email) throws SendByOpException;

    /**
     * Processes a payment for a booking
     * @param reservationId The ID of the booking
     * @param payment The payment details
     * @return Updated booking with payment status
     * @throws SendByOpException if booking not found or payment processing fails
     */
    BookingDto processPayment(int reservationId, PaymentDto payment) throws SendByOpException;

    /**
     * Récupère l'historique des paiements d'un client avec pagination
     * @param email Email du client
     * @param pageable Informations de pagination
     * @return Page d'historique de paiements
     * @throws SendByOpException si le client n'existe pas
     */
    Page<PaymentHistoryDto> getPaymentHistory(String email, Pageable pageable) throws SendByOpException;

    /**
     * Récupère l'historique complet des paiements d'un client
     * @param email Email du client
     * @return Liste de l'historique des paiements
     * @throws SendByOpException si le client n'existe pas
     */
    List<PaymentHistoryDto> getPaymentHistoryAll(String email) throws SendByOpException;

    /**
     * Récupère les statistiques de paiement d'un client
     * @param email Email du client
     * @return Statistiques de paiement
     * @throws SendByOpException si le client n'existe pas
     */
    PaymentStatsDto getPaymentStats(String email) throws SendByOpException;

    /**
     * DTO pour les statistiques de paiement
     */
    record PaymentStatsDto(
            long totalPayments,
            Double totalAmount,
            Double averageAmount
    ) {}
}
