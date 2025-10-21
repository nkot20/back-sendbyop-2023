package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.CancellationReservationDto;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.dto.CustomerBookingDto;
import com.sendByOP.expedition.models.dto.RejectionDto;
import com.sendByOP.expedition.services.iServices.IAnnulationReservationService;
import com.sendByOP.expedition.services.iServices.ICustomerService;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.reponse.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs for managing flight bookings and reservations")
public class BookingController {

    private final IReservationService bookingService;
    private final ICustomerService customerService;
    private final IAnnulationReservationService cancellationService;


    /**
     * Effectuer une réservation
     * @return
     * @throws SendByOpException
     */
    @Operation(summary = "Create a new booking", description = "Creates a new booking with associated parcels")
    @ApiResponse(responseCode = "201", description = "Booking created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid booking data")
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody @Valid BookingDto booking) throws SendByOpException {
        BookingDto newBooking = bookingService.saveBookingWithParcels(booking);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    /**
     * Modifier la réservation
     * @return
     * @throws SendByOpException
     */
    @Operation(summary = "Update a booking", description = "Updates an existing booking by ID")
    @ApiResponse(responseCode = "200", description = "Booking updated successfully")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@Parameter(description = "ID of the booking to update") @PathVariable("id") int id, @RequestBody @Valid BookingDto booking) throws SendByOpException {
        booking.setId(id);
        BookingDto updatedBooking = bookingService.updateBooking(booking);
        return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
    }

    /**
     * Annuler la réservation
     * @return
     * @throws SendByOpException
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable("id") int id, @RequestBody String reason) throws SendByOpException {
        BookingDto cancelledBooking = bookingService.cancelBooking(id, reason);
        return new ResponseEntity<>(cancelledBooking, HttpStatus.OK);
    }

    /**
     * Liste des réservations
     * @return
     */
    @Operation(summary = "Get all bookings", description = "Retrieves a list of all bookings in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of bookings")
    @GetMapping
    public ResponseEntity<?> getAllBookings() throws SendByOpException {
        List<BookingDto> bookings = bookingService.getAllBookings();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Liste des réservations d'un vol
     * @return
     */
    @GetMapping("/flights/{flightId}")
    public ResponseEntity<?> getBookingsByFlight(@PathVariable("flightId") int flightId) throws SendByOpException {
        List<BookingDto> bookings = bookingService.getAllBookings();
        bookings.removeIf(booking -> booking.getFlightId() != flightId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Liste des réservations d'un client destinateur
     * @param email
     * @return
     */
    @GetMapping("/receivers/{email}")
    public ResponseEntity<?> getBookingsByReceiver(@PathVariable("email") String email) throws SendByOpException {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        List<BookingDto> bookings = bookingService.getBookingsByReceiverId(customer.getId());
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Liste des réservations d'un client expediteur
     * @param email
     * @return
     */
    @GetMapping("/senders/{email}")
    public ResponseEntity<?> getBookingsBySender(@PathVariable("email") String email) throws SendByOpException {
        CustomerDto sender = customerService.getCustomerByEmail(email);
        List<BookingDto> bookings = bookingService.getAllBookings();
        //bookings.removeIf(booking -> !booking.getB().getSender().getEmail().equals(sender.getEmail()));
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * supprimer une réservation
     * @param id
     * @return
     * @throws SendByOpException
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable("id") int id) throws SendByOpException {
        bookingService.deleteBooking(id);
        return new ResponseEntity<>(new ResponseMessage("Booking deleted successfully"), HttpStatus.OK);
    }

    /**
     * détails d'une réservation
     * @param id
     * @return
     * @throws SendByOpException
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable("id") int id) throws SendByOpException {
        BookingDto booking = bookingService.getBooking(id);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }


    /**
     * valider réservation (1 pour valider)
     * @param id
     * @return
     * @throws jakarta.mail.MessagingException
     * @throws UnsupportedEncodingException
     * @throws SendByOpException
     */
    @PutMapping("/{id}/validate")
    public ResponseEntity<?> validateBooking(@PathVariable("id") int id) throws SendByOpException {
        BookingDto validatedBooking = bookingService.validateBooking(id);
        return new ResponseEntity<>(validatedBooking, HttpStatus.OK);
    }

    /**
     * valider réservation (2 pour refuser)
     * @param id
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws SendByOpException
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable("id") int id, @RequestBody RejectionDto rejection) throws SendByOpException, MessagingException, UnsupportedEncodingException {
        BookingDto booking = bookingService.getBooking(id);
        BookingDto rejectedBooking = bookingService.rejectBooking(booking, rejection);
        return new ResponseEntity<>(rejectedBooking, HttpStatus.OK);
    }

    /**
     * payer réservation
     * @param id
     * @return
     * @throws SendByOpException
     */
    @PostMapping("/{id}/payment")
    public ResponseEntity<?> processPayment(@PathVariable("id") int id) throws SendByOpException {
        BookingDto processedBooking = bookingService.processPayment(id);
        return new ResponseEntity<>(processedBooking, HttpStatus.OK);
    }

    /**
     * afficher le lien qui mène vers le groupe WhatsApp d'un expéditeur. Cela se fait après le paiement de la réservatoin
     * @return
     * @throws SendByOpException
     */
    @GetMapping("/{id}/sender/whatsapp")
    public ResponseEntity<?> getSenderWhatsAppLink(@PathVariable("id") int id) throws SendByOpException {
        String whatsappLink = bookingService.getSenderWhatsAppLink(id);
        return new ResponseEntity<>(whatsappLink, HttpStatus.OK);
    }


    /**
     * Ecrire un avis à l'expéditeur sur une réservation
     * @param id
     * @param opinion
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws SendByOpException
     */
    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addCustomerReview(@PathVariable("id") int id, @RequestBody String review) throws SendByOpException {
        BookingDto updatedBooking = bookingService.addCustomerReview(id, review);
        return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
    }

    //Répondre à un avis par l'expéditeur
    @PostMapping("/{id}/reviews/response")
    public ResponseEntity<?> addSenderResponse(@PathVariable("id") int id, @RequestBody String response) throws SendByOpException {
        BookingDto updatedBooking = bookingService.addSenderResponse(id, response);
        return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
    }


    @GetMapping("/{id}/cancellation")
    public ResponseEntity<?> getBookingCancellation(@PathVariable("id") int id) throws SendByOpException {
        CancellationReservationDto cancellation = cancellationService.findByReservation(id);
        return new ResponseEntity<>(cancellation, HttpStatus.OK);
    }

    /**
     * Get customer bookings by email with detailed information
     * @param email Customer email address
     * @return List of customer bookings with flight, receiver and parcel details
     * @throws SendByOpException
     */
    @Operation(summary = "Get customer bookings by email", 
               description = "Retrieves all bookings for a customer by email with detailed flight, receiver and parcel information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer bookings")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<CustomerBookingDto>> getCustomerBookingsByEmail(
            @Parameter(description = "Customer email address") @PathVariable("email") String email) throws SendByOpException {
        List<CustomerBookingDto> bookings = bookingService.getCustomerBookingsByEmail(email);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Get paginated customer bookings by email with detailed information
     * @param email Customer email address
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return Paginated list of customer bookings with flight, receiver and parcel details
     * @throws SendByOpException
     */
    @Operation(summary = "Get paginated customer bookings by email", 
               description = "Retrieves paginated bookings for a customer by email with detailed flight, receiver and parcel information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated customer bookings")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/customer/{email}/paginated")
    public ResponseEntity<Page<CustomerBookingDto>> getCustomerBookingsByEmailPaginated(
            @Parameter(description = "Customer email address") @PathVariable("email") String email,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-based)") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of items per page") int size) throws SendByOpException {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerBookingDto> bookings = bookingService.getCustomerBookingsByEmailPaginated(email, pageable);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

}
