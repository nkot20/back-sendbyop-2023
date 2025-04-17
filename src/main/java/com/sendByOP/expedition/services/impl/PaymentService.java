package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.mappers.InvoiceMapper;
import com.sendByOP.expedition.mappers.PaymentMapper;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.services.iServices.IParcelService;
import com.sendByOP.expedition.services.iServices.IPaymentService;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.BillRepository;
import com.sendByOP.expedition.repositories.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IParcelService parcelService;
    private final IReservationService reservationService;
    private final BillRepository factureRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final FlightService flightService;
    private final InvoiceMapper invoiceMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public BookingDto calculateInvoiceAmount(int bookingId) throws SendByOpException {
        log.debug("Calculating invoice amount for booking ID: {}", bookingId);
        BookingDto booking = reservationService.getBooking(bookingId);
        
        if (booking.getPaymentStatus() != 0) {

            FlightDto flight = flightService.getVolByIdVol(booking.getFlightId());
            
            if (flight.getKgCount() > 0) {
                int amountPerKg = flight.getAmountPerKg();
                List<ParcelDto> parcels = parcelService.findAllParcelsByBooking(booking);
                
                float totalAmount = parcels.stream()
                    .map(parcel -> parcel.getWeightKg() * amountPerKg)
                    .reduce(0f, Float::sum);

                float finalAmount = totalAmount + 3; // Service fee
                
                InvoiceDto invoiceDto = InvoiceDto.builder()
                    .amount(finalAmount)
                    .reservationId(booking.getId())
                    .build();
                Invoice newInvoice = factureRepository.save(invoiceMapper.toEntity(invoiceDto));
                
                PaymentDto payment = PaymentDto.builder()
                    .amount((double) finalAmount)
                    .paymentDate(new Date())
                    .clientId(booking.getCustomerId())
                    .paymentTypeId(3) // Standard payment type
                    .build();
                
                save(payment);
                
                log.info("Payment processed successfully for booking ID: {}, amount: {}", bookingId, finalAmount);
                //TODO: Calcul du pourcentage de sendByOp


                //TODO: Envoi de l'argent à l'expéditeur

                //TODO: Envoi de l'argent à SendByOp
                booking.setPaymentStatus(1);
                return reservationService.updateBooking(booking);
            } else {
                return null;
            }

        } else {
            return null;
        }

    }

    @Override
    public PaymentDto save(PaymentDto paymentDto) {
        Payment payment = paymentMapper.toEntity(paymentDto);
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentDto> getAll() {
        return paymentRepository.findAll().stream()
            .map(paymentMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDto> getPaymentsByClient(String email) throws SendByOpException {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        if (customer == null) {
            log.error("Customer not found with email: {}", email);
            throw new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND);
        }
        return paymentRepository.findByClient(customer).stream()
            .map(paymentMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public BookingDto processPayment(int reservationId, PaymentDto payment) throws SendByOpException {
        log.debug("Processing payment for reservation ID: {}", reservationId);
        
        BookingDto booking = reservationService.getBooking(reservationId);
        if (booking == null) {
            log.error("Booking not found with ID: {}", reservationId);
            throw new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND);
        }

        return calculateInvoiceAmount(reservationId);
    }
}
