package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.PaymentDto;
import com.sendByOP.expedition.models.dto.BookingDto;

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
}
