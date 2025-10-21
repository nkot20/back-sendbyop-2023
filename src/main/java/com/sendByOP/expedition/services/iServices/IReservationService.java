package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.RejectionDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.dto.CustomerBookingDto;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.List;

public interface IReservationService {

    public BookingDto saveBooking(BookingDto booking) throws SendByOpException;
    public BookingDto saveBookingWithParcels(BookingDto booking) throws SendByOpException;
    public void deleteBooking(int id) throws SendByOpException;
    public BookingDto updateBooking(BookingDto booking) throws SendByOpException;
    public BookingDto getBooking(int id) throws SendByOpException;
    public List<BookingDto> getAllBookings() throws SendByOpException;
    public List<BookingDto> getBookingsByReceiverId(int receiverId) throws SendByOpException;
    public List<BookingDto> getBookingsByDate(Date date) throws SendByOpException;
    public BookingDto rejectBooking(BookingDto booking, RejectionDto rejection)
            throws MessagingException, UnsupportedEncodingException, SendByOpException;
    public BookingDto validateBooking(int bookingId) throws SendByOpException;
    public BookingDto addCustomerReview(int bookingId, String review) throws SendByOpException;
    public BookingDto addSenderResponse(int bookingId, String response) throws SendByOpException;
    public BookingDto cancelBooking(int bookingId, String reason) throws SendByOpException;
    public String getSenderWhatsAppLink(int bookingId) throws SendByOpException;
    public BookingDto processPayment(int bookingId) throws SendByOpException;
    public List<CustomerBookingDto> getCustomerBookingsByEmail(String email) throws SendByOpException;
    public Page<CustomerBookingDto> getCustomerBookingsByEmailPaginated(String email, Pageable pageable) throws SendByOpException;

}
