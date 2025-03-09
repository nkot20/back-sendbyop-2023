package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RefundableBookingDto {
    private Integer id;
    private Integer bookingId;
    private Integer validated;
}
