package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.dto.RefundDto;
import com.sendByOP.expedition.services.impl.RefundService;
import com.sendByOP.expedition.services.impl.ReservationService;
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
@RequestMapping("reimbursement")
@RequiredArgsConstructor
@Tag(name = "Refunds", description = "API endpoints for managing refunds")
public class RefundController {
    private final RefundService refundService;
    private final ReservationService reservationService;

    @Operation(summary = "Create a new refund", description = "Creates a new refund request for a booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Refund created successfully",
                content = @Content(schema = @Schema(implementation = RefundDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(value = "/save")
    public ResponseEntity<?> save(@RequestBody @Parameter(description = "Refund details") RefundDto refund) throws SendByOpException {
        RefundDto savedRefund = refundService.save(refund);
        return new ResponseEntity<>(savedRefund, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all refunds", description = "Retrieves a list of all refund requests")
    @ApiResponse(responseCode = "200", description = "List of refunds retrieved successfully",
            content = @Content(schema = @Schema(implementation = RefundDto.class)))
    @GetMapping(value = "/list")
    public ResponseEntity<?> getAllRefunds() {
        List<RefundDto> refunds = refundService.getRefunds();
        return new ResponseEntity<>(refunds, HttpStatus.OK);
    }

    @Operation(summary = "Get refund by reservation", description = "Retrieves refund details for a specific reservation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund found",
                content = @Content(schema = @Schema(implementation = RefundDto.class))),
        @ApiResponse(responseCode = "404", description = "Refund not found")
    })
    @GetMapping(value = "/reservation/{id}")
    public ResponseEntity<?> getByReservation(@PathVariable("id") @Parameter(description = "Reservation ID") int id) throws SendByOpException {
        BookingDto reservation = reservationService.getBooking(id);
        RefundDto refund = refundService.findByReservation(reservation);
        return new ResponseEntity<>(refund, HttpStatus.OK);
    }
}
