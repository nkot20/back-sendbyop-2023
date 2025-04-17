package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.iServices.IParcelService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.ParcelDto;
import com.sendByOP.expedition.services.impl.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/parcels")
@RequiredArgsConstructor
public class ParcelController {
    private final IParcelService parcelService;
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ParcelDto> createParcel(@Valid @RequestBody ParcelDto parcel) throws SendByOpException {
        ParcelDto savedParcel = parcelService.saveParcel(parcel);
        return new ResponseEntity<>(savedParcel, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParcel(@PathVariable("id") int id) throws SendByOpException {
        parcelService.deleteParcelById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<List<ParcelDto>> getParcelsForBooking(@PathVariable("id") int id) throws SendByOpException {
        List<ParcelDto> parcels = parcelService.findAllParcelsByBookingId(id);
        return ResponseEntity.ok(parcels);
    }
}
