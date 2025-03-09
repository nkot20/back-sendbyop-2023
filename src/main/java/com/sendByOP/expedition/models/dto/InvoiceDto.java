package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class InvoiceDto {
    private Integer id;
    private Float amount;
    private Date paymentDate;
    private Integer reservationId;
}
