package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.iServices.IRefundableBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/people-reimbursed")
public class PersonneARembourserController {
    private final IRefundableBookingService reservationsARembourserService;

    @GetMapping(value = "/reservations/remboursements")
    public ResponseEntity<?> getAll() {
        return new ResponseEntity<>(reservationsARembourserService.findAll(), HttpStatus.OK);
    }

}
