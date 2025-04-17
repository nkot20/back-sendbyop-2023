package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.dto.RefundableBookingDto;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.iServices.IRefundableBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/refundable-bookings")
@Tag(name = "Refundable Bookings", description = "API endpoints for managing refundable bookings")
public class RefundableBookingController {
    private final IRefundableBookingService refundableBookingService;

    @Operation(summary = "Get all refundable bookings", description = "Retrieves a list of all refundable bookings")
    @ApiResponse(responseCode = "200", description = "List of refundable bookings retrieved successfully",
            content = @Content(schema = @Schema(implementation = RefundableBookingDto.class)))
    @GetMapping("/list")
    public ResponseEntity<?> getAllRefundableBookings() throws SendByOpException {
        List<RefundableBookingDto> refundableBookings = refundableBookingService.findAll();
        return new ResponseEntity<>(refundableBookings, HttpStatus.OK);
    }

    @Operation(summary = "Get refundable booking by ID", description = "Retrieves a specific refundable booking by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking found",
                content = @Content(schema = @Schema(implementation = BookingDto.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getRefundableBooking(@PathVariable("id") @Parameter(description = "Booking ID") int id) throws SendByOpException {
        BookingDto booking = refundableBookingService.findRefundableBooking(id);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

    @Operation(summary = "Create a new refundable booking", description = "Creates a new refundable booking entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Booking created successfully",
                content = @Content(schema = @Schema(implementation = RefundableBookingDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/save")
    public ResponseEntity<?> saveRefundableBooking(@RequestBody @Parameter(description = "Booking details") RefundableBookingDto booking) throws SendByOpException {
        RefundableBookingDto savedBooking = refundableBookingService.save(booking);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a refundable booking", description = "Deletes a refundable booking by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRefundableBooking(@PathVariable("id") @Parameter(description = "Booking ID") int id) throws SendByOpException {
        refundableBookingService.delete(id);
        return new ResponseEntity<>(new ResponseMessage("Refundable booking deleted successfully"), HttpStatus.OK);
    }
}
