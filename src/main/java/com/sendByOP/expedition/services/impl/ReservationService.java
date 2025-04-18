package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.ReservationRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService implements IReservationService {
    private final ReservationRepository bookingRepository;
    private final ParcelService parcelService;
    private final SendMailService emailService;
    private final RejectionService rejectionService;
    private final BookingMapper bookingMapper;
    private final CustomerService customerService;


    @Override
    public BookingDto saveBooking(BookingDto booking) throws SendByOpException {
        log.debug("Saving booking for customer ID: {}", booking.getCustomerId());
        try {
            CHeckNull.checkNumero(booking.getCustomerId());
            BookingDto savedBooking = bookingMapper.toDto(
                    bookingRepository.save(bookingMapper.toEntity(booking))
            );
            return savedBooking;
        } catch (Exception e) {
            log.error("Error saving booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto saveBookingWithParcels(BookingDto booking) throws SendByOpException {
        log.debug("Saving booking with parcels for customer ID: {}", booking.getCustomerId());
        try {
            initializeBookingStatus(booking);
            booking.setReceiverId(booking.getReceiverId());
            
            BookingDto newBooking = saveBooking(booking);
            
            for (ParcelDto parcel : booking.getParcelIds()) {
                try {
                    parcelService.saveParcel(parcel);
                } catch (SendByOpException e) {
                    log.error("Error saving parcel: {}", e.getMessage());
                    throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
                }
            }
            
            log.info("Successfully saved booking with parcels, booking ID: {}", newBooking.getId());
            return newBooking;
        } catch (Exception e) {
            log.error("Error saving booking with parcels: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }


    private void initializeBookingStatus(BookingDto booking) {
        booking.setPaymentStatus(0);
        booking.setSenderReceptionStatus(0);
        booking.setCustomerReview("");
        booking.setSenderReview("");
        booking.setCustomerReceptionStatus(0);
        booking.setCancelled(0);
        booking.setTransporterPaymentStatus(0);
        booking.setExpeditionStatus(0);
    }

    @Override
    public BookingDto updateBooking(BookingDto booking) throws SendByOpException {
        log.debug("Updating booking with ID: {}", booking.getId());
        try {
            BookingDto updatedBooking = saveBooking(booking);
            log.info("Successfully updated booking with ID: {}", updatedBooking.getId());
            return updatedBooking;
        } catch (Exception e) {
            log.error("Error updating booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public void deleteBooking(int id) throws SendByOpException {
        log.debug("Deleting booking with ID: {}", id);
        try {
            bookingRepository.deleteById(id);
            log.info("Successfully deleted booking with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto getBooking(int id) throws SendByOpException {
        log.debug("Fetching booking with ID: {}", id);
        try {
            BookingDto booking = bookingMapper.toDto(bookingRepository.findById(id)
                    .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND)));
            log.info("Successfully fetched booking with ID: {}", id);
            return booking;
        } catch (SendByOpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public List<BookingDto> getAllBookings() throws SendByOpException {
        log.debug("Fetching all bookings");
        try {
            List<BookingDto> bookings = bookingRepository.findAllByOrderByBookingDateDesc().stream()
                    .map(bookingMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Successfully fetched {} bookings", bookings.size());
            return bookings;
        } catch (Exception e) {
            log.error("Error fetching bookings: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public List<BookingDto> getBookingsByReceiverId(int receiverId) throws SendByOpException {
        log.debug("Fetching bookings for receiver ID: {}", receiverId);
        try {
            List<BookingDto> bookings = bookingRepository.findByCustomerIdOrderByBookingDateDesc(receiverId).stream()
                    .map(bookingMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Successfully fetched {} bookings for receiver ID: {}", bookings.size(), receiverId);
            return bookings;
        } catch (Exception e) {
            log.error("Error fetching receiver bookings: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public List<BookingDto> getBookingsByDate(Date date) throws SendByOpException {
        log.debug("Fetching bookings for date: {}", date);
        try {
            List<BookingDto> bookings = bookingRepository.findByBookingDate(date).stream()
                    .map(bookingMapper::toDto)
                    .collect(Collectors.toList());
            log.info("Successfully fetched {} bookings for date: {}", bookings.size(), date);
            return bookings;
        } catch (Exception e) {
            log.error("Error fetching bookings by date: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto rejectBooking(BookingDto booking, RejectionDto rejection) throws SendByOpException {
        log.debug("Rejecting booking with ID: {}", booking.getId());
        try {
            booking.setExpeditionStatus(2);
            BookingDto rejectedBooking = updateBooking(booking);
            
            rejection.setReservationId(booking.getId());
            rejectionService.saveRejection(rejection);
            
            String content = "Hello [[name]],<br>"
                    + "Your booking has been rejected by the sender.<br>"
                    + "Best regards,<br>"
                    + "The <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp</a></h3> team<br>";

            CustomerDto customerDto = customerService.getCustomerById(rejectedBooking.getCustomerId());

            emailService.simpleHtmlMessage(customerDto, content, "Booking Rejection");
            
            log.info("Successfully rejected booking with ID: {}", rejectedBooking.getId());
            return rejectedBooking;
        } catch (Exception e) {
            log.error("Error rejecting booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto processPayment(int bookingId) throws SendByOpException {
        log.debug("Processing payment for booking ID: {}", bookingId);
        try {
            BookingDto booking = getBooking(bookingId);
            if (booking == null) {
                throw new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND);
            }

            if (booking.getPaymentStatus() == 1) {
                throw new SendByOpException(ErrorInfo.PAYMENT_ALREADY_PROCESSED);
            }

            booking.setPaymentStatus(1);
            BookingDto updatedBooking = updateBooking(booking);

            String content = "Hello [[name]],<br>"
                    + "Your payment for booking ID " + bookingId + " has been processed successfully.<br>"
                    + "Best regards,<br>"
                    + "The <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp</a></h3> team<br>";

            CustomerDto customerDto = customerService.getCustomerById(updatedBooking.getCustomerId());
            emailService.simpleHtmlMessage(customerDto, content, "Payment Confirmation");

            log.info("Successfully processed payment for booking ID: {}", bookingId);
            return updatedBooking;
        } catch (SendByOpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto validateBooking(int bookingId) throws SendByOpException {
        log.debug("Validating booking with ID: {}", bookingId);
        try {
            BookingDto booking = getBooking(bookingId);
            if (booking.getExpeditionStatus() != 0) {
                throw new SendByOpException(ErrorInfo.OPERATION_NOT_ALLOWED);
            }

            booking.setExpeditionStatus(1);
            BookingDto validatedBooking = updateBooking(booking);

            String content = "Hello [[name]],<br>"
                    + "Your booking has been validated successfully.<br>"
                    + "Best regards,<br>"
                    + "The <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp</a></h3> team<br>";

            CustomerDto customerDto = customerService.getCustomerById(validatedBooking.getCustomerId());
            emailService.simpleHtmlMessage(customerDto, content, "Booking Validation");

            log.info("Successfully validated booking with ID: {}", bookingId);
            return validatedBooking;
        } catch (SendByOpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto addCustomerReview(int bookingId, String review) throws SendByOpException {
        log.debug("Adding customer review for booking ID: {}", bookingId);
        try {
            BookingDto booking = getBooking(bookingId);
            if (booking.getCustomerReceptionStatus() == 0) {
                throw new SendByOpException(ErrorInfo.OPERATION_NOT_ALLOWED);
            }
            booking.setCustomerReview(review);
            BookingDto updatedBooking = updateBooking(booking);

            String content = "Hello [[name]],<br>"
                    + "You have received a new review for your shipment: " + review + "<br>"
                    + "Best regards,<br>"
                    + "The <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp</a></h3> team<br>";

            CustomerDto senderDto = customerService.getCustomerById(updatedBooking.getCustomerId());
            emailService.simpleHtmlMessage(senderDto, content, "New Booking Review");

            return updatedBooking;
        } catch (Exception e) {
            log.error("Error adding customer review: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto addSenderResponse(int bookingId, String response) throws SendByOpException {
        log.debug("Adding sender response for booking ID: {}", bookingId);
        try {
            BookingDto booking = getBooking(bookingId);
            if (booking.getCustomerReceptionStatus() == 0) {
                throw new SendByOpException(ErrorInfo.OPERATION_NOT_ALLOWED);
            }
            booking.setSenderReview(response);
            BookingDto updatedBooking = updateBooking(booking);

            String content = "Hello [[name]],<br>"
                    + "The sender has responded to your review: " + response + "<br>"
                    + "Best regards,<br>"
                    + "The <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp</a></h3> team<br>";

            CustomerDto customerDto = customerService.getCustomerById(updatedBooking.getCustomerId());
            emailService.simpleHtmlMessage(customerDto, content, "Sender Response to Review");

            return updatedBooking;
        } catch (Exception e) {
            log.error("Error adding sender response: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public BookingDto cancelBooking(int bookingId, String reason) throws SendByOpException {
        log.debug("Cancelling booking with ID: {}", bookingId);
        try {
            BookingDto booking = getBooking(bookingId);
            booking.setCancelled(1);
            BookingDto cancelledBooking = updateBooking(booking);

            String content = "Hello [[name]],<br>"
                    + "Your booking has been cancelled. Reason: " + reason + "<br>"
                    + "Best regards,<br>"
                    + "The <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp</a></h3> team<br>";

            CustomerDto customerDto = customerService.getCustomerById(cancelledBooking.getCustomerId());
            emailService.simpleHtmlMessage(customerDto, content, "Booking Cancellation");

            return cancelledBooking;
        } catch (Exception e) {
            log.error("Error cancelling booking: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public String getSenderWhatsAppLink(int bookingId) throws SendByOpException {
        log.debug("Getting sender WhatsApp link for booking ID: {}", bookingId);
        try {
            BookingDto booking = getBooking(bookingId);
            if (booking.getPaymentStatus() == 0) {
                throw new SendByOpException(ErrorInfo.PAYMENT_REQUIRED);
            }
            CustomerDto sender = customerService.getCustomerById(booking.getCustomerId());
            return sender.getWhatsappLink();
        } catch (Exception e) {
            log.error("Error getting sender WhatsApp link: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

}
